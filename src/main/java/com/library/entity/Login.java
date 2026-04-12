package com.library.entity;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

@Data
@Entity
@Table(name = "login")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @OneToOne(mappedBy = "login", cascade = CascadeType.ALL)
    @JsonIgnore
    private User user;
    
    @OneToOne(mappedBy = "login", cascade = CascadeType.ALL)
    @JsonIgnore
    private Staff staff;
    
    @OneToOne(mappedBy = "login", cascade = CascadeType.ALL)
    @JsonIgnore
    private Admin admin;
    


    public enum Role {
        USER, STAFF, ADMIN
    }
}
