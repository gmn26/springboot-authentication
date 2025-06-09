package com.gmn26.springboot_authentication.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequestDto {
    private String name;

    private String email;

    private String username;

    private String password;
}
