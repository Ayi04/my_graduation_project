package com.library.service;

import com.library.entity.FrequentSeat;
import com.library.entity.User;
import java.util.List;

public interface FrequentSeatService {
    // 添加常用座位
    FrequentSeat addFrequentSeat(User user, Long seatId) throws Exception;
    
    // 删除常用座位
    void removeFrequentSeat(Long id);
    
    // 获取用户的常用座位列表
    List<FrequentSeat> getFrequentSeatsByUser(User user);
    
    // 检查用户是否可以添加常用座位（最多2个）
    boolean canAddFrequentSeat(User user);
    
    // 检查座位是否已添加为常用座位
    boolean isSeatFrequent(User user, Long seatId);
}
