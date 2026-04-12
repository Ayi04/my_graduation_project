package com.library.repository;

import com.library.entity.Seat;
import com.library.entity.Seat.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByArea_Id(Long areaId);
    List<Seat> findByArea_IdAndStatus(Long areaId, SeatStatus status);
    long countByStatus(SeatStatus status);
    long countByArea_Id(Long areaId);
    long countByArea_IdAndStatus(Long areaId, SeatStatus status);
}