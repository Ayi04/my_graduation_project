package com.library.controller;

import com.library.entity.Area;
import com.library.entity.Reservation;
import com.library.entity.Seat;
import com.library.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/seat")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    // 获取所有区域
    @GetMapping("/areas")
    public List<Area> getAllAreas() {
        return seatService.getAllAreas();
    }
    
    // 根据楼层获取区域
    @GetMapping("/areas/{floor}")
    public List<Area> getAreasByFloor(@PathVariable Integer floor) {
        return seatService.getAreasByFloor(floor);
    }
    
    // 获取区域的座位
    @GetMapping("/seats/{areaId}")
    public List<Seat> getSeatsByAreaId(@PathVariable Long areaId) {
        return seatService.getSeatsByAreaId(areaId);
    }
    
    // 获取区域的可用座位
    @GetMapping("/availableSeats/{areaId}")
    public List<Seat> getAvailableSeatsByAreaId(@PathVariable Long areaId) {
        return seatService.getAvailableSeatsByAreaId(areaId);
    }
    
    // 预约座位
    @PostMapping("/reserve")
    public Reservation reserveSeat(@RequestBody ReserveRequest request) {
        return seatService.reserveSeat(request.getLoginId(), request.getSeatId(), request.getStartTime(), request.getEndTime());
    }
    
    // 预约请求参数
    static class ReserveRequest {
        private Long loginId;
        private Long seatId;
        private Date startTime;
        private Date endTime;
        
        public Long getLoginId() {
            return loginId;
        }
        
        public void setLoginId(Long loginId) {
            this.loginId = loginId;
        }
        
        public Long getSeatId() {
            return seatId;
        }
        
        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }
        
        public Date getStartTime() {
            return startTime;
        }
        
        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }
        
        public Date getEndTime() {
            return endTime;
        }
        
        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }
    }
    
    // 取消预约
    @PostMapping("/cancel")
    public boolean cancelReservation(@RequestParam Long reservationId, @RequestParam Long loginId) {
        return seatService.cancelReservation(reservationId, loginId);
    }
    
    // 签到
    @PostMapping("/checkIn")
    public boolean checkIn(@RequestParam Long reservationId, @RequestParam Long loginId) {
        return seatService.checkIn(reservationId, loginId);
    }
    
    // 结束使用
    @PostMapping("/endUse")
    public boolean endUse(@RequestParam Long reservationId, @RequestParam Long loginId) {
        return seatService.endUse(reservationId, loginId);
    }
    
    // 删除预约
    @DeleteMapping("/delete")
    public boolean deleteReservation(@RequestParam Long reservationId, @RequestParam Long loginId) {
        return seatService.deleteReservation(reservationId, loginId);
    }
    
    // 获取用户的预约记录
    @GetMapping("/reservations/login/{loginId}")
    public List<Reservation> getLoginReservations(@PathVariable Long loginId) {
        return seatService.getLoginReservations(loginId);
    }
    
    // 获取所有预约记录
    @GetMapping("/reservations")
    public List<Reservation> getAllReservations() {
        return seatService.getAllReservations();
    }
    
    // 根据区域ID获取座位（用于座位管理）
    @GetMapping("/area/{areaId}")
    public List<Seat> getSeatsByAreaForManagement(@PathVariable Long areaId) {
        return seatService.getSeatsByAreaId(areaId);
    }
    
    // 添加座位
    @PostMapping("/")
    public Seat addSeat(@RequestBody Seat seat) {
        return seatService.addSeat(seat);
    }
    
    // 更新座位
    @PutMapping("/{seatId}")
    public Seat updateSeat(@PathVariable Long seatId, @RequestBody Seat seat) {
        return seatService.updateSeat(seatId, seat);
    }
    
    // 删除座位
    @DeleteMapping("/{seatId}")
    public boolean deleteSeat(@PathVariable Long seatId) {
        return seatService.deleteSeat(seatId);
    }
    
    // 添加区域
    @PostMapping("/area")
    public Area addArea(@RequestBody Area area) {
        return seatService.addArea(area);
    }
    
    // 更新区域
    @PutMapping("/area/{areaId}")
    public Area updateArea(@PathVariable Long areaId, @RequestBody Area area) {
        return seatService.updateArea(areaId, area);
    }
    
    // 删除区域
    @DeleteMapping("/area/{areaId}")
    public boolean deleteArea(@PathVariable Long areaId) {
        return seatService.deleteArea(areaId);
    }
    
    // 更新预约状态
    @PutMapping("/reservation/{reservationId}")
    public boolean updateReservationStatus(@PathVariable Long reservationId, @RequestBody ReservationStatusUpdateRequest request) {
        return seatService.updateReservationStatus(reservationId, request.getStatus());
    }
    
    // 预约状态更新请求参数
    static class ReservationStatusUpdateRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}