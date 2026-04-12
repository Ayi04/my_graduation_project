package com.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "frequent_seat", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "seat_id"}))
public class FrequentSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 关联用户
    
    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat; // 关联座位
}
