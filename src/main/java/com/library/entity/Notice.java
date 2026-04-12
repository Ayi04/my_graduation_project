package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title; // 公告标题
    private String content; // 公告内容
    private Date publishTime; // 发布时间
    private String publisher; // 发布者
    private Boolean isActive = true; // 是否有效
}