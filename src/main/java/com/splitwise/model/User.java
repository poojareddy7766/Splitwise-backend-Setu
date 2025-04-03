package com.splitwise.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")  
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp  
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  
}
