package com.gmn26.springboot_authentication.service;

import com.gmn26.springboot_authentication.dto.auth.VerifyRequestDto;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OTPService{
    private static final Integer EXPIRES_IN_MINUTES = 3;
    private final LoadingCache<String, Integer> otpCache;

    public OTPService() {
        super();
        otpCache = CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRES_IN_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public int generateOTP(String key) {
        Random random = new Random();
        int otp = random.nextInt(999999);
        otpCache.put(key, otp);
        return otp;
    }

    public boolean verifyOTP(VerifyRequestDto verifyRequestDto) {
        Integer cachedOtp = getOTP(verifyRequestDto.getEmail());
        if (cachedOtp != null && cachedOtp.equals(verifyRequestDto.getOtp())) {
            clearOTP(verifyRequestDto.getEmail());
            return true;
        }
        return false;
    }

    public Integer getOTP(String key) {
        return otpCache.getUnchecked(key);
    }

    public void clearOTP(String key) {
        otpCache.invalidate(key);
    }
}
