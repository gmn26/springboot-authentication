package com.gmn26.springboot_authentication.service;

import com.gmn26.springboot_authentication.JWTProvider.JwtTokenProvider;
import com.gmn26.springboot_authentication.dto.auth.LoginRequestDto;
import com.gmn26.springboot_authentication.dto.auth.RegisterRequestDto;
import com.gmn26.springboot_authentication.dto.auth.VerifyRequestDto;
import com.gmn26.springboot_authentication.entity.UserEntity;
import com.gmn26.springboot_authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final OTPService otpService;

    private final EmailVerificationService emailVerificationService;

    private final EmailService emailService;

    @Override
    public Optional<String> getEmailByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::getEmail);
    }

    @Override
    public String loginViaUsername(LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtTokenProvider.getJwtSecret(authentication);
        } catch (AuthenticationException e) {
            log.error("Authentication failed error : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String loginViaEmail(LoginRequestDto loginRequestDto){
        boolean validEmail = userRepository.existsByEmailAndEmailVerifiedTrue(loginRequestDto.getEmail());

        String errorMessage = "Something went wrong, please try again";

        if(validEmail){
            String email = loginRequestDto.getEmail();

            int otp = otpService.generateOTP(email);

            String subject = "OTP Verification - Springboot Authentication";
            String message = String.valueOf(otp);

            try{
                emailService.sendOtp(email, subject, message);
                return "OTP sended to your email, please verify to continue!";
            }catch (Exception e){
                log.error("Failed to send OTP: {}",e.getMessage());
                return errorMessage;
            }
        }else {
            RegisterRequestDto registerRequestDto = new RegisterRequestDto();
            registerRequestDto.setEmail(loginRequestDto.getEmail());
            boolean autoRegister = registerViaEmail(registerRequestDto);
            if(autoRegister){
                return "You're email haven't been approved by our system, please check your email to finish verification!";
            } else{
                return errorMessage;
            }
        }
    }

    public String verifyOtpLogin(VerifyRequestDto verifyRequestDto){
        boolean approvedOtp = otpService.verifyOTP(verifyRequestDto);

        if(!approvedOtp){
            return "";
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        verifyRequestDto.getEmail(),
                        verifyRequestDto.getOtp()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtTokenProvider.getJwtSecret(authentication);
    }

    @Override
    public Boolean registerViaUsername(RegisterRequestDto registerRequestDto) {
        Boolean isExists = userRepository.existsByUsername(registerRequestDto.getUsername());

        if(isExists) {
            return false;
        }

        String encodedPassword = passwordEncoder.encode(registerRequestDto.getPassword());

        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerRequestDto.getUsername());
        userEntity.setEmail(registerRequestDto.getEmail());
        userEntity.setUsername(registerRequestDto.getUsername());
        userEntity.setPassword(encodedPassword);
        userRepository.save(userEntity);

        return true;
    }

    @Override
    public Boolean registerViaEmail(RegisterRequestDto registerRequestDto){
        Boolean isExistsAndValidated = userRepository.existsByEmailAndEmailVerifiedTrue(registerRequestDto.getEmail());

        if(isExistsAndValidated) {
            return false;
        }

        String token = emailVerificationService.generateToken(registerRequestDto.getEmail());

        String subject = "Email Verification - Springboot Authentication";
        String message = String.valueOf(token);

        try{
            emailService.sendToken(registerRequestDto.getEmail(), subject, message);
        }catch (Exception e){
            log.error("Failed to send Token: {}",e.getMessage());
        }

        Boolean isExists = userRepository.existsByEmail(registerRequestDto.getEmail());

        if(!isExists) {
            UserEntity userEntity = new UserEntity();

            String name = registerRequestDto.getEmail().split("@")[0];

            userEntity.setName(name);
            userEntity.setEmail(registerRequestDto.getEmail());
            userEntity.setEmailVerified(false);
            userRepository.save(userEntity);
        }

        return true;
    }
}
