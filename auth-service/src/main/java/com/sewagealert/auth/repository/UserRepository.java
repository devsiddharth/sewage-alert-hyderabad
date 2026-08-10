package com.sewagealert.auth.repository;

import com.sewagealert.auth.model.Role;
import com.sewagealert.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    // findByRole: Lists all users with a given role (e.g. FIELD_OFFICER for assignment).
    List<User> findByRole(Role role);
}
