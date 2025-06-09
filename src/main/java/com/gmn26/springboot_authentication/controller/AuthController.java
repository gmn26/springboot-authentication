package com.gmn26.springboot_authentication.controller;

import com.gmn26.springboot_authentication.dto.WebResponse;
import com.gmn26.springboot_authentication.dto.auth.LoginRequestDto;
import com.gmn26.springboot_authentication.dto.auth.LoginResponse;
import com.gmn26.springboot_authentication.dto.auth.RegisterRequestDto;
import com.gmn26.springboot_authentication.dto.auth.VerifyRequestDto;
import com.gmn26.springboot_authentication.service.AuthServiceImpl;
import com.gmn26.springboot_authentication.service.EmailVerificationService;
import com.gmn26.springboot_authentication.service.OTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    private final OTPService otpService;

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    public ResponseEntity<WebResponse<LoginResponse>> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponse loginResponse = new LoginResponse();
        WebResponse<LoginResponse> response;
        if(loginRequestDto.getUsername() != null || loginRequestDto.getPassword() != null) {
            String token = authService.loginViaUsername(loginRequestDto);
            loginResponse.setToken(token);

            if (token == null) {
                response = WebResponse.<LoginResponse>builder()
                        .success(false)
                        .message("Invalid username or password")
                        .data(null)
                        .build();
            } else {
                response = WebResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Login successful, welcome!.")
                        .data(loginResponse)
                        .build();
            }
        }else {
            String loginStatus = authService.loginViaEmail(loginRequestDto);

            response = WebResponse.<LoginResponse>builder()
                    .success(true)
                    .message(loginStatus)
                    .build();
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<WebResponse<String>> verifyOTP(@RequestBody VerifyRequestDto verifyRequestDto) {
        String token = authService.verifyOtpLogin(verifyRequestDto);

        WebResponse<String> response;

        if(token != null) {
            response = WebResponse.<String>builder()
                    .success(true)
                    .message("Login success, welcome!")
                    .data(token)
                    .build();
        } else {
            response = WebResponse.<String>builder()
                    .success(true)
                    .message("Wrong OTP, try again!")
                    .build();
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<WebResponse<LoginResponse>> register(@RequestBody RegisterRequestDto registerRequestDto) {
        WebResponse<LoginResponse> response;
        if(registerRequestDto.getUsername() != null && registerRequestDto.getPassword() != null) {
            Boolean registerResponse = authService.registerViaUsername(registerRequestDto);
            if (!registerResponse) {
                response = WebResponse.<LoginResponse>builder()
                        .success(false)
                        .message("User already exists")
                        .data(null)
                        .build();
            } else {
                LoginRequestDto loginRequestDto = new LoginRequestDto();
                loginRequestDto.setUsername(registerRequestDto.getUsername());
                loginRequestDto.setPassword(registerRequestDto.getPassword());

                LoginResponse loginResponse = new LoginResponse();

                String token = authService.loginViaUsername(loginRequestDto);

                loginResponse.setToken(token);

                response = WebResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Registration successful, welcome!")
                        .data(loginResponse)
                        .build();}
        }else {
            Boolean registerResponse = authService.registerViaEmail(registerRequestDto);

            if(registerResponse) {
                response = WebResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Registration successful, please check your email address!")
                        .build();
            } else {
                response = WebResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Email already registered!")
                        .build();
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/validate-email")
    public ResponseEntity<Void> validateEmail(
            @RequestParam("email") String email,
            @RequestParam("token") String token
    ) {
        VerifyRequestDto verifyRequestDto = new VerifyRequestDto();
        verifyRequestDto.setEmail(email);
        verifyRequestDto.setToken(token);
        boolean isValid = emailVerificationService.verifyToken(verifyRequestDto);

        String redirectUrl = isValid
                ? "www.google.com"
                : "www.youtube.com";

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
