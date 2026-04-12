package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String gender;
    private String phone;
    private String email;
    private String responsibleArea; // 负责区域，如"1楼"、"2楼"等
    private String avatar; // 头像路径
    
    @OneToOne
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private Login login;
}
