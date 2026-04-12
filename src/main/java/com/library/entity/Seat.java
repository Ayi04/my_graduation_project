package com.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String seatNumber; // 座位编号
    
    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area; // 所属区域
    
    // 用于接收前端传递的区域ID
    @Transient
    private Long areaId;
    
    @Enumerated(EnumType.STRING)
    private SeatStatus status; // 座位状态
    
    public enum SeatStatus {
        AVAILABLE, RESERVED, OCCUPIED
    }
}