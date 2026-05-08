package com.group32.cpt202.LY_contributor.repository;

import com.group32.cpt202.LY_contributor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole(User.Role role);

    java.util.List<User> findAllByOrderByIdAsc();

    java.util.List<User> findByRoleOrderByIdAsc(User.Role role);
}
