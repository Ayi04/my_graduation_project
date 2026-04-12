package com.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "area")
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; // 区域名称
    private Integer floor; // 楼层
    
    @Enumerated(EnumType.STRING)
    private AreaType type; // 区域类型
    
    private Integer seatCount; // 座位数
    private Integer availableCount; // 可用座位数
    private String openTime; // 开放时间
    private Integer minCreditScore; // 最低信用分要求
    private Integer checkinTimeLimit; // 签到时间限制（分钟）
    
    public enum AreaType {
        SILENT, BROWSING, NORMAL
    }
}