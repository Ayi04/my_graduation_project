package com.library.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "rule")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Integer sortOrder;
}
