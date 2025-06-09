package com.gmn26.springboot_authentication.service;

import com.gmn26.springboot_authentication.dto.auth.VerifyRequestDto;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EmailVerificationService {
    private static final Integer EXPIRATION_TIME_IN_MINUTES = 3;
    private final LoadingCache<String, String> emailVerificationToken;

    public EmailVerificationService() {
        super();
        emailVerificationToken = CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRATION_TIME_IN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    public String load(String key) throws Exception {
                        return "";
                    }
                });
    }

    public String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        emailVerificationToken.put(email, token);
        return token;
    }

    public boolean verifyToken(VerifyRequestDto verifyRequestDto) {
        String cachedToken = getToken(verifyRequestDto.getEmail());
        if (cachedToken != null && cachedToken.equals(verifyRequestDto.getToken())) {
            clearToken(verifyRequestDto.getEmail());
            return true;
        }
        return false;
    }

    public String getToken(String email) { return emailVerificationToken.getUnchecked(email); }

    public void clearToken(String email) { emailVerificationToken.invalidate(email); }
}
