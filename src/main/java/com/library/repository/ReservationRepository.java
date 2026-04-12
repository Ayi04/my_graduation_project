package com.library.repository;

import com.library.entity.Reservation;
import com.library.entity.Reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Date;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByLoginId(Long loginId);
    List<Reservation> findBySeatIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(Long seatId, ReservationStatus status, Date startTime, Date endTime);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByStatusNot(ReservationStatus status);
    List<Reservation> findBySeatIdAndStatus(Long seatId, ReservationStatus status);
    List<Reservation> findBySeatIdAndStatusIn(Long seatId, List<ReservationStatus> statuses);
}