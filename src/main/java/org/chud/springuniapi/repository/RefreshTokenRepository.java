package org.chud.springuniapi.repository;

import java.time.Instant;
import java.util.Optional;
import org.chud.springuniapi.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByKeyHash(String keyHash);

    // One UPDATE instead of loading every row. flushAutomatically = ture
    // pushes any canges in the caller's persistence context before the method runs
    // and then clearAutomatically = true says to
    //clear the persistence context after executing the bulk query.
    @Modifying(flushAutomatically = true ,clearAutomatically = true)
    @Query("""
            update RefreshToken r
               set r.revokedAt = :now
             where r.user.id = :userId
               and r.revokedAt is null
            """)
    void revokeAllLiveForUser(Long userId, Instant now);

    long deleteByExpiresAtBefore(Instant cutoff);

    long deleteByUserId(Long userId);

}
