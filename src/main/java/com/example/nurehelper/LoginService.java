package com.example.nurehelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
    private final NureApiService apiService;
    private String loginToken;

    @Scheduled(fixedRate = 1000 * 60 * 10)
    private void checkIn() {
        String response = apiService.getStringForUrl("https://dl.nure.ua/login/index.php");
        
        loginToken = extractLoginToken(response);
        log.info(loginToken);
    }

    private String extractLoginToken(String response) {
        if (response.contains("logintoken")) {
            int position = response.indexOf("logintoken") + 19;
            return response.substring(position, position + 32);
        }
        throw new RuntimeException("Couldn't retrieve login token from url.");
    }
}
