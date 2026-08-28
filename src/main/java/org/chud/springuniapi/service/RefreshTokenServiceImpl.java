package org.chud.springuniapi.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.chud.springuniapi.dto.RotationResult;
import org.chud.springuniapi.entity.RefreshToken;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.exception.InvalidRefreshTokenException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.repository.RefreshTokenRepository;
import org.chud.springuniapi.repository.UserRepository;
import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.security.MyUserDetailsService;
import org.chud.springuniapi.service.serviceInterface.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private static final int KEY_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration lifetime;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
        @Value("${user.jwt.refresh-ttl-days}") long lifetimeDays,
        MyUserDetailsService myUserDetailsService, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.lifetime = Duration.ofDays(lifetimeDays);
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String issueFor(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String raw = randomKey();
        refreshTokenRepository.save(new RefreshToken(
            hash(raw), user, Instant.now().plus(lifetime)
        ));

        return raw;
    }

    @Override
    //The if checks throw InvalidRefreshTokenException which would mark the
    //transaction as rollback-only which would rollback the revokeAllLiveForUser method
    //and would leave the token alive.
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RotationResult rotate(String presentKey) {

        Instant now = Instant.now();

        RefreshToken stored = refreshTokenRepository.findByKeyHash(hash(presentKey))
            .orElseThrow(() -> new InvalidRefreshTokenException("unknown key"));

        if (stored.getReplacedByHash() != null) {
            refreshTokenRepository.revokeAllLiveForUser(stored.getUser().getId(), now);

            throw new InvalidRefreshTokenException("Attempted rotation on already rotated token");
        }

        if (!stored.isLive(now)) {
            throw new InvalidRefreshTokenException("Token is no longer alive");
        }

        User user = stored.getUser();

        if (user.isDeleted()) {
            refreshTokenRepository.revokeAllLiveForUser(user.getId(), now);
            throw new InvalidRefreshTokenException("User is suspended");
        }

        String replacement = randomKey();
        String replacementHash = hash(replacement);
        stored.rotateInto(replacementHash, now);
        refreshTokenRepository.save(new RefreshToken(replacementHash, user, now.plus(lifetime)));

        MyUserDetails principal = (MyUserDetails)
            myUserDetailsService.loadUserByUsername(user.getEmail());

        return new RotationResult(principal, replacement, now.plus(lifetime));
    }

    @Override
    @Transactional
    public void revokeSingle(String presentKey) {
        refreshTokenRepository.findByKeyHash(hash(presentKey))
            .ifPresent(key -> key.revoke(Instant.now()));
    }

    @Override
    @Transactional
    public void revokeAllFor(Long userId) {
        refreshTokenRepository.revokeAllLiveForUser(userId, Instant.now());
    }

    //Generate random key
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
