package com.the.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.the.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    @Query(value = "select r from Role r inner join UserHasRole ur on r.id = ur.role.id where ur.id = :userId")
    List<String> findAllRolesByUserId(Long userId);
}
