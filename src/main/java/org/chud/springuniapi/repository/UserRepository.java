package org.chud.springuniapi.repository;

import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.repository.projection.CourseSummaryRow;
import org.chud.springuniapi.repository.projection.UserDisplayView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "courses")
    Optional<User> findWithCoursesById(Long id);

    //Open projection here due to the UserDisplayView. Difference between
    //this and closed projection is that Spring loads the whole entity since
    //it does not know what you want from it.
    List<UserDisplayView> findAllProjectedBy();

    List<User> findUsersByDeleted(boolean isDeleted);

    //The soft delete filter lives here instead of in the mapper. enabled = null
    //means "do not filter", which is what the ?enabled query param does when it is absent.
    @Query("""
            select new org.chud.springuniapi.repository.projection.CourseSummaryRow(s.id, c.id, c.name)
            from User s
            join s.courses c
            where s.id in :userIds
              and (:deleted is null or c.deleted = :deleted)
            order by s.id, c.id
            """)
    List<CourseSummaryRow> findCourseSummariesByUserIds(Collection<Long> userIds, Boolean deleted);

    Optional<User> findUserByEmailIgnoreCase(String email);

    @Query("select u.role.roleName from User u where u.id = :id")
    Optional<RoleName> findRoleNameByUserId(Long id);
}
