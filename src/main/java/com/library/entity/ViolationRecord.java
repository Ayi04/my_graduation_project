package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "violation_record")
public class ViolationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String type; // VIOLATION: 违规扣除信用分, BAN: 禁止预约
    private String reason; // 违规原因或禁止原因
    private Integer scoreChange; // 信用分变化，正数为增加，负数为扣除
    private Integer banDays; // 禁止天数
    private Date createTime; // 记录创建时间
    private Date banEndTime; // 禁止结束时间
}