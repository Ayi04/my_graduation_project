package com.library.service;

import com.library.entity.Reservation;
import com.library.entity.Seat;
import com.library.entity.Area;
import com.library.repository.ReservationRepository;
import com.library.repository.SeatRepository;
import com.library.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReservationCheckService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private UserService userService;

    // 每1分钟检查一次超时未签到的预约
    @Scheduled(cron = "0 * * * * ?")
    public void checkOverdueReservations() {
        Date now = new Date();
        // 查找所有待签到的预约
        List<Reservation> overdueReservations = reservationRepository.findByStatus(Reservation.ReservationStatus.CONFIRMED);

        // 获取签到时间限制（从第一个区域获取，因为所有区域的设置都相同）
        Integer checkinTimeLimit = 15; // 默认15分钟
        List<Area> areas = areaRepository.findAll();
        if (!areas.isEmpty()) {
            Area area = areas.get(0);
            if (area.getCheckinTimeLimit() != null) {
                checkinTimeLimit = area.getCheckinTimeLimit();
            }
        }

        for (Reservation reservation : overdueReservations) {
            // 检查是否超过开始时间指定分钟数
            if (now.getTime() - reservation.getStartTime().getTime() > checkinTimeLimit * 60 * 1000) {
                // 取消预约
                cancelOverdueReservation(reservation);
            }
        }
    }

    private void cancelOverdueReservation(Reservation reservation) {
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

        // 扣除信用分5分
        userService.updateCreditScore(reservation.getLogin().getId(), -5);
    }
}
