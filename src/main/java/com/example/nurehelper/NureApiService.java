package com.example.nurehelper;

import com.example.nurehelper.dto.LoginDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NureApiService {
    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    @PostConstruct
    private void init() {
        headers = new HttpHeaders();
    }

    public LoginDTO getLoginData(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return LoginDTO.builder()
                .loginToken(extractLoginToken(Objects.requireNonNull(response.getBody())))
                .cookie(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .build();
    }

    public void sendLoginRequest(String url, LoginDTO loginDTO) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Set-Cookie", loginDTO.getCookie());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("logintoken", loginDTO.getLoginToken());
        params.add("username", loginDTO.getLogin());
        params.add("password", loginDTO.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println(response.getBody());
    }

    private String extractLoginToken(String response) {
        if (response.contains("logintoken")) {
            int position = response.indexOf("logintoken") + 19;
            return response.substring(position, position + 32);
        }
        throw new RuntimeException("Couldn't retrieve login token from url.");
    }
}
