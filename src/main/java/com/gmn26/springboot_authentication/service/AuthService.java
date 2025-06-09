package com.gmn26.springboot_authentication.service;

import com.gmn26.springboot_authentication.dto.auth.LoginRequestDto;
import com.gmn26.springboot_authentication.dto.auth.RegisterRequestDto;

import java.util.Optional;

public interface AuthService {
    public String loginViaUsername(LoginRequestDto loginRequestDto);

    public String loginViaEmail(LoginRequestDto loginRequestDto);

    public Boolean registerViaUsername(RegisterRequestDto registerRequestDto);

    public Boolean registerViaEmail(RegisterRequestDto registerRequestDto);

    public Optional<String> getEmailByUsername(String username);
}
