package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login; // 预约用户
    
    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat; // 预约座位
    
    private Date reservationTime; // 预约时间
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
    
    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 预约状态
    
    private Date checkInTime; // 签到时间
    private Boolean isLate = false; // 是否迟到
    
    public enum ReservationStatus {
        CONFIRMED, CANCELLED, COMPLETED, ENDED
    }
}