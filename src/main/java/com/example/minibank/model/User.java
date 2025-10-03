package com.example.minibank.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    private String username;


    @Column(nullable = false)
    private String password;


    @Column(unique = true , nullable = false)
    private String email;


    private LocalDateTime createdAt= LocalDateTime.now();


    @Enumerated(EnumType.STRING)
    private Role  role = Role.USER;


    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
    }
}
