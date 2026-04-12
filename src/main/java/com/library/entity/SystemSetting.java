package com.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "system_setting")
public class SystemSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String settingKey; // 设置键
    private String value; // 设置值
}
