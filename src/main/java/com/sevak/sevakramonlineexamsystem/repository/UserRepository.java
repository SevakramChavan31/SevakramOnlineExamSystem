package com.sevak.sevakramonlineexamsystem.repository;

import com.sevak.sevakramonlineexamsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // =========================================================
    // FIND USER BY EMAIL
    // =========================================================

    Optional<User> findByEmail(String email);


    // =========================================================
    // CHECK DUPLICATE EMAIL
    // =========================================================

    boolean existsByEmail(String email);


    // =========================================================
    // COUNT USERS BY ROLE
    // =========================================================

    long countByRole(User.Role role);


    // =========================================================
    // COUNT USERS CREATED FROM SPECIFIC DATE
    // =========================================================

    long countByRoleAndCreatedAtGreaterThanEqual(
            User.Role role,
            LocalDateTime startDate
    );


    // =========================================================
    // FIND USERS BY ROLE
    // =========================================================

    List<User> findByRole(User.Role role);


    // =========================================================
    // SEARCH STUDENTS
    // =========================================================

    List<User>
    findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            User.Role role1,
            String fullName,
            User.Role role2,
            String email
    );
}