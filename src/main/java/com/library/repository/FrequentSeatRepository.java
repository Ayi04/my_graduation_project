package com.library.repository;

import com.library.entity.FrequentSeat;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentSeatRepository extends JpaRepository<FrequentSeat, Long> {
    // 根据用户查询常用座位
    List<FrequentSeat> findByUser(User user);
    
    // 根据用户和座位查询是否已存在
    boolean existsByUserAndSeatId(User user, Long seatId);
    
    // 根据用户ID统计常用座位数量
    long countByUserId(Long userId);
}
