package com.gmn26.springboot_authentication.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyRequestDto {
    private String email;

    private int otp;

    private String token;
}
