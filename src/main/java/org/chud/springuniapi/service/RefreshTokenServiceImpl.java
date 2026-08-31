package org.chud.springuniapi.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import org.chud.springuniapi.dto.RotationResult;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.exception.InvalidRefreshTokenException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.repository.UserRepository;
import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.security.MyUserDetailsService;
import org.chud.springuniapi.service.serviceInterface.IRefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private static final int KEY_BYTES = 32;
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    //using string constants instead of writing them in the methods because a typo
    //could compile and result in wrong added field in redis
    private static final String TOKEN_PREFIX = "refresh:"; //key name prefix for token
    private static final String USER_PREFIX = "refresh:user:"; //key name prefix for user
    //fields
    private static final String F_USER_ID = "userId";
    private static final String F_EXPIRES_AT = "expiresAt";
    private static final String F_REVOKED_AT = "revokedAt";
    private static final String F_REPLACED_BY = "replacedByHash";

    //spring's redis client object. configured to send and receive plain text
    private final StringRedisTemplate redis;
    //the Redis key is a String, the hash field names are String, the hash values are String
    private final HashOperations<String, String, String> hashOps;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration lifetime;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;

    public RefreshTokenServiceImpl(
        StringRedisTemplate redis,
        @Value("${user.jwt.refresh-ttl-days}") long lifetimeDays,
        MyUserDetailsService myUserDetailsService,
        UserRepository userRepository) {
        this.redis = redis;
        //returns wrapper that makes the calls into hash
        // commands, doing it here rather than calling this method in every method
        this.hashOps = redis.opsForHash();
        this.lifetime = Duration.ofDays(lifetimeDays);
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public String issueFor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        String raw = randomKey();
        store(hash(raw), userId, Instant.now().plus(lifetime));
        return raw;
    }

    @Override
    public RotationResult rotate(String presentKey) {
        Instant now = Instant.now();
        String key = tokenKey(hash(presentKey));

        Map<String, String> stored = hashOps.entries(key);
        if (stored.isEmpty()) {
            //Expired keys get removed by Redis, so expired and not stored are the
            //same case here. Both are a 401 anyway.
            throw new InvalidRefreshTokenException("not stored or expired key");
        }

        Long userId = Long.valueOf(stored.get(F_USER_ID));

        if (stored.get(F_REPLACED_BY) != null) {
            revokeAllFor(userId);
            log.warn("Refresh token reuse detected for user {} - all sessions revoked", userId);
            throw new InvalidRefreshTokenException("Attempted rotation on already rotated token");
        }

        if (stored.get(F_REVOKED_AT) != null) {
            throw new InvalidRefreshTokenException("Token is no longer alive");
        }

        String replacement = randomKey();
        String replacementHash = hash(replacement);

        //Exactly one concurrent caller can claim this token. Losing the race means somebody
        //already rotated it, which means the token has leaked.
        Boolean claimed = hashOps.putIfAbsent(key, F_REPLACED_BY, replacementHash);
        if (!claimed) {
            revokeAllFor(userId);
            log.warn("Concurrent rotation of one refresh token for user {} - all sessions revoked", userId);
            throw new InvalidRefreshTokenException("Attempted rotation on already rotated token");
        }

        //add revoked at field
        hashOps.putIfAbsent(key, F_REVOKED_AT, now.toString());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidRefreshTokenException("Owner no longer exists"));

        if (user.isDeleted()) {
            revokeAllFor(userId);
            throw new InvalidRefreshTokenException("User is suspended");
        }

        Instant expiresAt = now.plus(lifetime);

        //store the new refresh token with the updated hash
        store(replacementHash, userId, expiresAt);

        MyUserDetails principal = (MyUserDetails)
            myUserDetailsService.loadUserByUsername(user.getEmail());

        return new RotationResult(principal, replacement, expiresAt);
    }

    @Override
    public void revokeSingle(String presentKey) {
        String key = tokenKey(hash(presentKey));
        if (redis.hasKey(key)) {
            //putIfAbsent so a second logout does not overwrite the first timestamp
            hashOps.putIfAbsent(key, F_REVOKED_AT, Instant.now().toString());
        }
    }

    @Override
    public void revokeAllFor(Long userId) {
        String index = userKey(userId);
        Set<String> hashes = redis.opsForSet().members(index);
        if (hashes == null || hashes.isEmpty()) {
            return;
        }

        String now = Instant.now().toString();
        for (String tokenHash : hashes) {
            String key = tokenKey(tokenHash);
            if (redis.hasKey(key)) {
                hashOps.putIfAbsent(key, F_REVOKED_AT, now);
            } else {
                redis.opsForSet().remove(index, tokenHash); //already expired
            }
        }
    }

    //Writes the token hash and its reverse index entry, both on the same TTL.
    private void store(String tokenHash, Long userId, Instant expiresAt) {
        //create the key, and store it in redis with userId and expiresAt + set ttl
        String key = tokenKey(tokenHash);
        hashOps.putAll(key, Map.of(
            F_USER_ID, String.valueOf(userId),
            F_EXPIRES_AT, expiresAt.toString()));
        redis.expire(key, lifetime);

        //add the refresh token to the user and give it its ttl
        String index = userKey(userId);
        redis.opsForSet().add(index, tokenHash);
        redis.expire(index, lifetime);
    }

    //create token string helper
    private String tokenKey(String tokenHash) {
        return TOKEN_PREFIX + tokenHash;
    }

    //create user string helper
    private String userKey(Long userId) {
        return USER_PREFIX + userId;
    }

    //Generate random key, produces the actual secret handed to the client
    private String randomKey() {
        //allocate a 32 byte buffer, filled with zeros
        byte[] bytes = new byte[KEY_BYTES];

        //fills the buffer, we use SecureRandom instead of random because
        //random is actually very predictable, with only 2 values you can predict
        //any other future value
        secureRandom.nextBytes(bytes);

        //turns 32 raw bytes into a 43-character printable string
        //use UrlEncoder so it makes a url safe alphabet
        //strips the trailing = characters.
        // 32 bytes isn't a multiple of 3, so standard Base64 would append =
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    //It turns the secret into a 64-character SHA-256 code.
    // The same token always creates the same Redis key, but because the
    // code can’t easily be reversed, someone who gets access to the database
    // can’t use it to recover the original token.
    private String hash(String raw) {
        try {

            //gets sha-256 which converts input into a unique 256 bit code
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            //renders those 32 bytes as 64 lowercase hex characters
            //it feeds the bytes in, finalizes, and returns the 32-byte digest
            //converts the Base64 string to bytes,
            // because a digest consumes bytes and not characters
            return HexFormat.of()
                .formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the JDK", e);
        }
    }
}
