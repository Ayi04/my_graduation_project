package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.text.SimpleDateFormat;

@Service
public class SeatService {
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private AreaRepository areaRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemSettingRepository systemSettingRepository;
    
    // 获取所有区域
    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }
    
    // 根据楼层获取区域
    public List<Area> getAreasByFloor(Integer floor) {
        return areaRepository.findByFloor(floor);
    }
    
    // 获取区域的座位
    public List<Seat> getSeatsByAreaId(Long areaId) {
        return seatRepository.findByArea_Id(areaId);
    }
    
    // 获取区域的可用座位
    public List<Seat> getAvailableSeatsByAreaId(Long areaId) {
        return seatRepository.findByArea_IdAndStatus(areaId, Seat.SeatStatus.AVAILABLE);
    }
    
    // 预约座位
    public Reservation reserveSeat(Long loginId, Long seatId, Date startTime, Date endTime) {
        // 检查图书馆是否处于关闭状态
        if (isLibraryClosed()) {
            return null;
        }
        
        // 检查用户是否可以预约
        if (!userService.canReserve(loginId)) {
            return null;
        }
        
        // 获取用户信息
        User user = userService.getUserByLoginId(loginId);
        if (user == null) {
            return null;
        }
        
        // 获取登录信息
        Login login = userService.getLoginById(loginId);
        if (login == null) {
            return null;
        }
        
        // 获取座位信息
        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null || seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            return null;
        }
        
        // 检查用户信用分是否满足区域要求
        Area area = seat.getArea();
        if (user.getCreditScore() < area.getMinCreditScore()) {
            return null;
        }
        
        // 检查预约时间是否在区域开放时间内
        if (!isWithinOpenHours(startTime, endTime, area.getOpenTime())) {
            return null;
        }
        
        // 检查座位是否在预约时间段内已被预约
        List<Reservation> existingReservations = reservationRepository.findBySeatIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                seatId, Reservation.ReservationStatus.CONFIRMED, endTime, startTime);
        if (!existingReservations.isEmpty()) {
            return null;
        }
        
        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setLogin(login);
        reservation.setSeat(seat);
        reservation.setReservationTime(new Date());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        
        // 更新座位状态
        seat.setStatus(Seat.SeatStatus.RESERVED);
        seatRepository.save(seat);
        
        // 更新区域可用座位数
        area.setAvailableCount(area.getAvailableCount() - 1);
        areaRepository.save(area);
        
        return reservationRepository.save(reservation);
    }
    
    // 取消预约
    public boolean cancelReservation(Long reservationId, Long loginId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null || !reservation.getLogin().getId().equals(loginId)) {
            return false;
        }
        
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            return false;
        }
        
        // 更新预约状态
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        
        // 更新座位状态
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        
        // 更新区域可用座位数
        Area area = seat.getArea();
        area.setAvailableCount(area.getAvailableCount() + 1);
        areaRepository.save(area);
        
        // 取消预约不影响信用分
        return true;
    }
    
    // 签到
    public boolean checkIn(Long reservationId, Long loginId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null || !reservation.getLogin().getId().equals(loginId)) {
            return false;
        }
        
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            return false;
        }
        
        Date now = new Date();
        // 检查是否迟到（超过预约开始时间15分钟）
        boolean isLate = now.getTime() - reservation.getStartTime().getTime() > 15 * 60 * 1000;
        
        // 更新预约信息
        reservation.setCheckInTime(now);
        reservation.setIsLate(isLate);
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        
        // 更新座位状态
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);
        
        // 更新信用分
        if (isLate) {
            // 迟到扣5分
            userService.updateCreditScore(loginId, -5);
        } else {
            // 按时签到加2分
            userService.updateCreditScore(loginId, 2);
        }
        
        return true;
    }
    
    // 结束使用
    public boolean endUse(Long reservationId, Long loginId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null || !reservation.getLogin().getId().equals(loginId)) {
            return false;
        }
        
        if (reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
            return false;
        }
        
        // 更新预约状态为已结束
        reservation.setStatus(Reservation.ReservationStatus.ENDED);
        reservationRepository.save(reservation);
        
        // 更新座位状态
        Seat seat = reservation.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        
        // 更新区域可用座位数
        Area area = seat.getArea();
        area.setAvailableCount(area.getAvailableCount() + 1);
        areaRepository.save(area);
        
        // 按时结束使用加1分
        userService.updateCreditScore(loginId, 1);
        
        return true;
    }
    
    // 获取用户的预约记录
    public List<Reservation> getLoginReservations(Long loginId) {
        return reservationRepository.findByLoginId(loginId);
    }
    
    // 获取所有预约记录
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    
    // 根据ID获取预约
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId).orElse(null);
    }
    
    // 删除预约
    public boolean deleteReservation(Long reservationId, Long loginId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return false;
        }
        
        // 检查用户角色
        Login login = userService.getLoginById(loginId);
        if (login == null) {
            return false;
        }
        
        // 普通用户只能删除自己的预约，且只有已取消或已完成的预约才能删除
        if (login.getRole() == Login.Role.USER) {
            if (!reservation.getLogin().getId().equals(loginId)) {
                return false;
            }
            if (reservation.getStatus() != Reservation.ReservationStatus.CANCELLED && 
                reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
                return false;
            }
        }
        // 工作人员和管理员可以删除任何预约
        
        // 保存座位信息，用于后续更新
        Seat seat = reservation.getSeat();
        Area area = seat.getArea();
        
        // 删除预约
        reservationRepository.delete(reservation);
        
        // 更新座位状态和区域可用座位数
        // 如果预约状态是已确认或已完成，需要恢复座位状态
        if ((reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED || 
             reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) && 
            (seat.getStatus() == Seat.SeatStatus.RESERVED || 
             seat.getStatus() == Seat.SeatStatus.OCCUPIED)) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seatRepository.save(seat);
            
            area.setAvailableCount(area.getAvailableCount() + 1);
            areaRepository.save(area);
        }
        
        return true;
    }
    
    // 检查预约时间是否在区域开放时间内
    private boolean isWithinOpenHours(Date startTime, Date endTime, String openTime) {
        if (openTime == null || openTime.isEmpty()) {
            // 如果没有设置开放时间，默认允许预约
            return true;
        }
        
        try {
            // 解析开放时间字符串，格式如 "08:00-22:00"
            String[] timeRange = openTime.split("-");
            if (timeRange.length != 2) {
                return true;
            }
            
            String[] startTimeParts = timeRange[0].split(":");
            String[] endTimeParts = timeRange[1].split(":");
            
            if (startTimeParts.length != 2 || endTimeParts.length != 2) {
                return true;
            }
            
            int openHour = Integer.parseInt(startTimeParts[0]);
            int openMinute = Integer.parseInt(startTimeParts[1]);
            int closeHour = Integer.parseInt(endTimeParts[0]);
            int closeMinute = Integer.parseInt(endTimeParts[1]);
            
            // 创建开放时间和关闭时间的Date对象
            Date openTimeDate = new Date();
            openTimeDate.setHours(openHour);
            openTimeDate.setMinutes(openMinute);
            openTimeDate.setSeconds(0);
            
            Date closeTimeDate = new Date();
            closeTimeDate.setHours(closeHour);
            closeTimeDate.setMinutes(closeMinute);
            closeTimeDate.setSeconds(0);
            
            // 调整开始时间和结束时间的日期部分，只保留时间部分进行比较
            Date checkStartTime = new Date();
            checkStartTime.setHours(startTime.getHours());
            checkStartTime.setMinutes(startTime.getMinutes());
            checkStartTime.setSeconds(0);
            
            Date checkEndTime = new Date();
            checkEndTime.setHours(endTime.getHours());
            checkEndTime.setMinutes(endTime.getMinutes());
            checkEndTime.setSeconds(0);
            
            // 检查预约时间是否在开放时间范围内
            return !checkStartTime.before(openTimeDate) && !checkEndTime.after(closeTimeDate);
        } catch (Exception e) {
            // 解析失败时默认允许预约
            return true;
        }
    }
    
    // 检查图书馆是否处于关闭状态
    private boolean isLibraryClosed() {
        try {
            // 获取系统设置
            SystemSetting isLibraryClosedSetting = systemSettingRepository.findBySettingKey("isLibraryClosed");
            SystemSetting closeStartTimeSetting = systemSettingRepository.findBySettingKey("closeStartTime");
            SystemSetting closeEndTimeSetting = systemSettingRepository.findBySettingKey("closeEndTime");
            
            // 检查是否设置了关闭图书馆
            if (isLibraryClosedSetting == null || !"true".equals(isLibraryClosedSetting.getValue())) {
                return false;
            }
            
            // 检查是否设置了关闭时间
            if (closeStartTimeSetting == null || closeEndTimeSetting == null) {
                return false;
            }
            
            String closeStartTimeStr = closeStartTimeSetting.getValue();
            String closeEndTimeStr = closeEndTimeSetting.getValue();
            
            if (closeStartTimeStr == null || closeStartTimeStr.isEmpty() || closeEndTimeStr == null || closeEndTimeStr.isEmpty()) {
                return false;
            }
            
            // 解析关闭时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date closeStartTime = sdf.parse(closeStartTimeStr);
            Date closeEndTime = sdf.parse(closeEndTimeStr);
            Date now = new Date();
            
            // 检查当前时间是否在关闭时间范围内
            return now.compareTo(closeStartTime) >= 0 && now.compareTo(closeEndTime) <= 0;
        } catch (Exception e) {
            // 解析失败时默认允许预约
            return false;
        }
    }
    
    // 添加座位
    public Seat addSeat(Seat seat) {
        Area area = areaRepository.findById(seat.getAreaId()).orElse(null);
        if (area == null) {
            return null;
        }
        
        seat.setArea(area);
        Seat savedSeat = seatRepository.save(seat);
        
        // 更新区域座位数
        area.setSeatCount(area.getSeatCount() + 1);
        if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
            area.setAvailableCount(area.getAvailableCount() + 1);
        }
        areaRepository.save(area);
        
        return savedSeat;
    }
    
    // 更新座位
    public Seat updateSeat(Long seatId, Seat seat) {
        Seat existingSeat = seatRepository.findById(seatId).orElse(null);
        if (existingSeat == null) {
            return null;
        }
        
        // 检查座位状态是否变更
        boolean statusChanged = existingSeat.getStatus() != seat.getStatus();
        Seat.SeatStatus oldStatus = existingSeat.getStatus();
        
        // 更新座位信息
        existingSeat.setSeatNumber(seat.getSeatNumber());
        existingSeat.setStatus(seat.getStatus());
        
        // 保存更新后的座位
        Seat updatedSeat = seatRepository.save(existingSeat);
        
        // 如果状态变更，更新区域的可用座位数
        if (statusChanged) {
            Area area = existingSeat.getArea();
            if (oldStatus == Seat.SeatStatus.AVAILABLE) {
                area.setAvailableCount(area.getAvailableCount() - 1);
            } else if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                area.setAvailableCount(area.getAvailableCount() + 1);
                
                // 如果座位从已预约改为可用，取消相应的预约
                List<Reservation> confirmedReservations = reservationRepository.findBySeatIdAndStatus(seatId, Reservation.ReservationStatus.CONFIRMED);
                for (Reservation reservation : confirmedReservations) {
                    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                }
                
                // 如果座位从已占用改为可用，将相应的预约改为已结束
                List<Reservation> completedReservations = reservationRepository.findBySeatIdAndStatus(seatId, Reservation.ReservationStatus.COMPLETED);
                for (Reservation reservation : completedReservations) {
                    reservation.setStatus(Reservation.ReservationStatus.ENDED);
                    reservationRepository.save(reservation);
                }
            } else if (oldStatus == Seat.SeatStatus.OCCUPIED && seat.getStatus() == Seat.SeatStatus.RESERVED) {
                // 如果座位从已占用改为已预约，将相应的预约改为待签到
                List<Reservation> completedReservations = reservationRepository.findBySeatIdAndStatus(seatId, Reservation.ReservationStatus.COMPLETED);
                for (Reservation reservation : completedReservations) {
                    reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
                    reservation.setCheckInTime(null); // 清除签到时间
                    reservation.setIsLate(false); // 重置迟到标记
                    reservationRepository.save(reservation);
                }
            }
            areaRepository.save(area);
        }
        
        return updatedSeat;
    }
    
    // 删除座位
    public boolean deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null) {
            return false;
        }
        
        // 检查座位是否有未完成的预约
        List<Reservation> activeReservations = reservationRepository.findBySeatIdAndStatusIn(seatId, 
            Arrays.asList(Reservation.ReservationStatus.CONFIRMED, Reservation.ReservationStatus.COMPLETED));
        if (!activeReservations.isEmpty()) {
            return false;
        }
        
        // 保存区域信息，用于后续更新
        Area area = seat.getArea();
        
        // 删除座位
        seatRepository.delete(seat);
        
        // 更新区域座位数
        area.setSeatCount(area.getSeatCount() - 1);
        if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
            area.setAvailableCount(area.getAvailableCount() - 1);
        }
        areaRepository.save(area);
        
        return true;
    }
    
    // 更新预约状态
    public boolean updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return false;
        }
        
        try {
            Reservation.ReservationStatus newStatus = Reservation.ReservationStatus.valueOf(status);
            
            // 保存旧状态
            Reservation.ReservationStatus oldStatus = reservation.getStatus();
            
            // 更新预约状态
            reservation.setStatus(newStatus);
            
            // 如果状态改为已签到，添加签到时间
            if (newStatus == Reservation.ReservationStatus.COMPLETED && oldStatus != Reservation.ReservationStatus.COMPLETED) {
                reservation.setCheckInTime(new Date());
                reservation.setIsLate(false); // 工作人员手动签到，不视为迟到
            }
            
            // 保存更新后的预约
            reservationRepository.save(reservation);
            
            // 更新座位状态
            Seat seat = reservation.getSeat();
            if (seat != null) {
                if (newStatus == Reservation.ReservationStatus.CONFIRMED) {
                    seat.setStatus(Seat.SeatStatus.RESERVED);
                } else if (newStatus == Reservation.ReservationStatus.COMPLETED) {
                    seat.setStatus(Seat.SeatStatus.OCCUPIED);
                } else if (newStatus == Reservation.ReservationStatus.CANCELLED || newStatus == Reservation.ReservationStatus.ENDED) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    
                    // 更新区域可用座位数
                    Area area = seat.getArea();
                    if (area != null) {
                        area.setAvailableCount(area.getAvailableCount() + 1);
                        areaRepository.save(area);
                    }
                }
                seatRepository.save(seat);
            }
            
            return true;
        } catch (IllegalArgumentException e) {
            // 无效的状态值
            return false;
        }
    }
    
    // 添加区域
    public Area addArea(Area area) {
        // 初始化座位数和可用座位数为0
        area.setSeatCount(0);
        area.setAvailableCount(0);
        return areaRepository.save(area);
    }
    
    // 更新区域
    public Area updateArea(Long areaId, Area area) {
        Area existingArea = areaRepository.findById(areaId).orElse(null);
        if (existingArea == null) {
            return null;
        }
        
        // 更新区域信息
        existingArea.setName(area.getName());
        existingArea.setFloor(area.getFloor());
        existingArea.setType(area.getType());
        existingArea.setOpenTime(area.getOpenTime());
        existingArea.setMinCreditScore(area.getMinCreditScore());
        existingArea.setCheckinTimeLimit(area.getCheckinTimeLimit());
        
        return areaRepository.save(existingArea);
    }
    
    // 删除区域
    public boolean deleteArea(Long areaId) {
        Area area = areaRepository.findById(areaId).orElse(null);
        if (area == null) {
            return false;
        }
        
        // 检查区域是否有座位
        List<Seat> seats = seatRepository.findByArea_Id(areaId);
        if (!seats.isEmpty()) {
            // 删除区域内的所有座位
            for (Seat seat : seats) {
                // 检查座位是否有未完成的预约
                List<Reservation> activeReservations = reservationRepository.findBySeatIdAndStatusIn(seat.getId(), 
                    Arrays.asList(Reservation.ReservationStatus.CONFIRMED, Reservation.ReservationStatus.COMPLETED));
                if (!activeReservations.isEmpty()) {
                    return false; // 有未完成的预约，不能删除
                }
                seatRepository.delete(seat);
            }
        }
        
        // 删除区域
        areaRepository.delete(area);
        return true;
    }
}