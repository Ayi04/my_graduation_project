package com.library.repository;

import com.library.entity.Checkin;
import com.library.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {
    List<Checkin> findByLogin(Login login);
    List<Checkin> findAllByOrderByCheckinTimeDesc();
}