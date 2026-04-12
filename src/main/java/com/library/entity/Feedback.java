package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "feedback_complaint")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title; // 反馈标题
    
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login; // 提交用户
    
    private String type; // 反馈类型：feedback(意见反馈)或complaint(投诉)
    private String targetType; // 投诉对象类型：ADMIN(管理员)或STAFF(工作人员)
    private Long targetId; // 投诉对象ID
    private String content; // 反馈内容
    private Date submitTime; // 提交时间
    private Boolean isProcessed = false; // 是否处理
    private String processResult; // 处理结果
    @ManyToOne
    @JoinColumn(name = "processor_id")
    private Login processor; // 处理人
    private Date processTime; // 处理时间
}
