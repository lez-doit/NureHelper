package com.example.nurehelper;

import com.example.nurehelper.dto.LoginDTO;
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

    @Value("${api.login-url}")
    private String loginUrl;

    @Scheduled(fixedRate = 1000 * 60 * 10)
    private void checkIn() {
        LoginDTO loginDTO = apiService.getLoginData(loginUrl);

        log.info("Got login token: {}", loginDTO.getLoginToken());
        log.info("Got Set-Cookie: {}", loginDTO.getCookie());

        loginDTO.setLogin(userLogin);
        loginDTO.setPassword(userPassword);

        apiService.sendLoginRequest(loginUrl, loginDTO);
    }
}
