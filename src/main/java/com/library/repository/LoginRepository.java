package com.library.repository;

import com.library.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Login findByUsername(String username);
    List<Login> findByUsernameContaining(String username);
    List<Login> findByRole(Login.Role role);
    List<Login> findByUsernameContainingAndRole(String username, Login.Role role);
}
