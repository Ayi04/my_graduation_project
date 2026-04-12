package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String gender;
    private String phone;
    private String email;
    private String avatar; // 头像路径
    
    @OneToOne
    @JoinColumn(name = "login_id")
    @JsonIgnore
    private Login login;
}
