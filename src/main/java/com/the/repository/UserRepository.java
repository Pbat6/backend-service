package com.the.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.the.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions rp " +
            "LEFT JOIN FETCH rp.permission " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithAuthorities(@Param("username") String username);

    boolean existsByEmail(String email);

    @Query(value = "select r from Role r inner join UserHasRole ur on r.id = ur.role.id where ur.id = :userId")
    List<String> findAllRolesByUserId(Long userId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles uhr WHERE uhr.role.name = :roleName AND u.status = 'ACTIVE'")
    Page<User> findUsersByRoleName(String roleName, Pageable pageable);
}
