package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String gender;
    private String phone;
    private String email;
    
    // 信用分相关字段
    private Integer creditScore = 100; // 初始信用分100
    private Boolean isBanned = false; // 是否被禁止预约
    private Long banEndTime; // 禁止预约结束时间
    
    private String avatar; // 头像路径
    
    @OneToOne
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private Login login;
}
