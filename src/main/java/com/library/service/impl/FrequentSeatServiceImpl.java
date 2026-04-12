package com.library.service.impl;

import com.library.entity.FrequentSeat;
import com.library.entity.Seat;
import com.library.entity.User;
import com.library.repository.FrequentSeatRepository;
import com.library.repository.SeatRepository;
import com.library.service.FrequentSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrequentSeatServiceImpl implements FrequentSeatService {
    
    @Autowired
    private FrequentSeatRepository frequentSeatRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Override
    public FrequentSeat addFrequentSeat(User user, Long seatId) throws Exception {
        // 检查用户是否可以添加常用座位（最多2个）
        if (!canAddFrequentSeat(user)) {
            throw new Exception("常用座位数量已达上限（最多2个）");
        }
        
        // 检查座位是否已存在
        if (isSeatFrequent(user, seatId)) {
            throw new Exception("该座位已添加为常用座位");
        }
        
        // 检查座位是否存在
        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null) {
            throw new Exception("座位不存在");
        }
        
        // 创建常用座位记录
        FrequentSeat frequentSeat = new FrequentSeat();
        frequentSeat.setUser(user);
        frequentSeat.setSeat(seat);
        
        return frequentSeatRepository.save(frequentSeat);
    }
    
    @Override
    public void removeFrequentSeat(Long id) {
        frequentSeatRepository.deleteById(id);
    }
    
    @Override
    public List<FrequentSeat> getFrequentSeatsByUser(User user) {
        return frequentSeatRepository.findByUser(user);
    }
    
    @Override
    public boolean canAddFrequentSeat(User user) {
        return frequentSeatRepository.countByUserId(user.getId()) < 2;
    }
    
    @Override
    public boolean isSeatFrequent(User user, Long seatId) {
        return frequentSeatRepository.existsByUserAndSeatId(user, seatId);
    }
}
