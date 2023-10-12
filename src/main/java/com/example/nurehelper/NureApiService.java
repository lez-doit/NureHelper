package com.example.nurehelper;

import com.example.nurehelper.dto.LoginDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
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

    private String cookie;

    @PostConstruct
    private void init() {
        headers = new HttpHeaders();
    }

    public LoginDTO getLoginData(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return LoginDTO.builder()
                       .loginToken(extractLoginToken(Objects.requireNonNull(response.getBody())))
                       .cookie(response.getHeaders()
                                       .getFirst(HttpHeaders.SET_COOKIE))
                       .build();
    }

    public void sendLoginRequest(String url, LoginDTO loginDTO) {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", loginDTO.getCookie());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("logintoken", loginDTO.getLoginToken());
        params.add("username", loginDTO.getLogin());
        params.add("password", loginDTO.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println(response.getBody());
        cookie = response.getHeaders()
                         .getFirst("Set-Cookie");

        if (response.getStatusCode() == HttpStatus.SEE_OTHER) {
            String redirectUrl = Objects.requireNonNull(response.getHeaders()
                                                                .getLocation())
                                        .toString();
            headers = new HttpHeaders();
            headers.set("Cookie", cookie);
            request = new HttpEntity<>(headers);
            ResponseEntity<String> redirectedResponse = restTemplate.exchange(
                    redirectUrl,
                    HttpMethod.GET,
                    request,
                    String.class);
            System.out.println(redirectedResponse.getBody());
        }
    }

    private String extractLoginToken(String response) {
        if (response.contains("logintoken")) {
            int position = response.indexOf("logintoken") + 19;
            return response.substring(position, position + 32);
        }
        throw new RuntimeException("Couldn't retrieve login token from url.");
    }
}
