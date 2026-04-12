package com.library.repository;

import com.library.entity.ViolationRecord;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViolationRecordRepository extends JpaRepository<ViolationRecord, Long> {
    List<ViolationRecord> findByUser(User user);
}