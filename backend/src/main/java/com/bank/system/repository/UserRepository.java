package com.bank.system.repository;

import com.bank.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByLoginId(String loginId);
    List<User> findByRoleNot(String role);
}
