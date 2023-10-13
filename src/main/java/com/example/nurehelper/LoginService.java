package com.example.nurehelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
    private final NureApiService apiService;

    @Value("${user.login}")
    private String userLogin;

    @Value("${user.password}")
    private String userPassword;

    @Scheduled(fixedRate = 1000 * 60 * 10)
    private void checkIn() {
        apiService.sendLoginRequest(userLogin, userPassword);
    }
}
