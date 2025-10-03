package com.example.minibank.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    public  String email;
    public  String password;
}
