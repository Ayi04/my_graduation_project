package com.library.controller;

import com.library.entity.Checkin;
import com.library.entity.Login;
import com.library.entity.Reservation;
import com.library.repository.CheckinRepository;
import com.library.repository.LoginRepository;
import com.library.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {
    
    @Autowired
    private CheckinRepository checkinRepository;
    
    @Autowired
    private LoginRepository loginRepository;
    
    @Autowired
    private SeatService seatService;
    
    // 完成签到
    @PostMapping
    public CheckinResponse checkin(@RequestBody CheckinRequest checkinRequest) {
        Login login = loginRepository.findById(checkinRequest.getLoginId()).orElse(null);
        if (login == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 调用 SeatService 完成签到，更新预约状态和座位状态
        boolean success = seatService.checkIn(checkinRequest.getReservationId(), checkinRequest.getLoginId());
        if (!success) {
            throw new RuntimeException("签到失败");
        }
        
        // 获取预约信息
        Reservation reservation = seatService.getReservationById(checkinRequest.getReservationId());
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        Checkin checkin = new Checkin();
        checkin.setLogin(login);
        checkin.setReservation(reservation);
        checkin.setCheckinTime(new Date());
        
        Checkin savedCheckin = checkinRepository.save(checkin);
        return new CheckinResponse(savedCheckin);
    }
    
    // 签到请求参数
    static class CheckinRequest {
        private Long loginId;
        private Long reservationId;
        
        public Long getLoginId() {
            return loginId;
        }
        
        public void setLoginId(Long loginId) {
            this.loginId = loginId;
        }
        
        public Long getReservationId() {
            return reservationId;
        }
        
        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }
    }
    
    // 签到响应
    static class CheckinResponse {
        private Long id;
        private String username;
        private Long reservationId;
        private Date checkinTime;
        
        public CheckinResponse(Checkin checkin) {
            this.id = checkin.getId();
            this.username = checkin.getLogin().getUsername();
            this.reservationId = checkin.getReservation().getId();
            this.checkinTime = checkin.getCheckinTime();
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Long getReservationId() {
            return reservationId;
        }
        
        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }
        
        public Date getCheckinTime() {
            return checkinTime;
        }
        
        public void setCheckinTime(Date checkinTime) {
            this.checkinTime = checkinTime;
        }
    }
    
    // 签到记录详情响应
    static class CheckinDetailResponse {
        private Long id;
        private String username;
        private Long reservationId;
        private Date checkinTime;
        private Integer floor;
        private String areaName;
        private String seatNumber;
        
        public CheckinDetailResponse(Checkin checkin) {
            this.id = checkin.getId();
            this.username = checkin.getLogin().getUsername();
            this.reservationId = checkin.getReservation().getId();
            this.checkinTime = checkin.getCheckinTime();
            
            if (checkin.getReservation().getSeat() != null) {
                this.seatNumber = checkin.getReservation().getSeat().getSeatNumber();
                if (checkin.getReservation().getSeat().getArea() != null) {
                    this.floor = checkin.getReservation().getSeat().getArea().getFloor();
                    this.areaName = checkin.getReservation().getSeat().getArea().getName();
                }
            }
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Long getReservationId() {
            return reservationId;
        }
        
        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }
        
        public Date getCheckinTime() {
            return checkinTime;
        }
        
        public void setCheckinTime(Date checkinTime) {
            this.checkinTime = checkinTime;
        }
        
        public Integer getFloor() {
            return floor;
        }
        
        public void setFloor(Integer floor) {
            this.floor = floor;
        }
        
        public String getAreaName() {
            return areaName;
        }
        
        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }
        
        public String getSeatNumber() {
            return seatNumber;
        }
        
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }
    }
    
    // 获取用户的签到记录
    @GetMapping("/login/{loginId}")
    public List<CheckinDetailResponse> getLoginCheckinRecords(@PathVariable Long loginId) {
        Login login = loginRepository.findById(loginId).orElse(null);
        if (login == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return checkinRepository.findByLogin(login).stream()
                .map(CheckinDetailResponse::new)
                .collect(Collectors.toList());
    }
    
    // 获取所有签到记录（供工作人员使用）
    @GetMapping
    public List<CheckinDetailResponse> getAllCheckinRecords() {
        return checkinRepository.findAll().stream()
                .map(CheckinDetailResponse::new)
                .collect(Collectors.toList());
    }
    
    // 删除签到记录（供工作人员使用）
    @DeleteMapping("/{id}")
    public boolean deleteCheckin(@PathVariable Long id) {
        try {
            checkinRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}