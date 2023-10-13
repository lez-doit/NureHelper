package com.example.nurehelper;

import com.example.nurehelper.config.SessionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final SessionConfig sessionConfig;

    @Value("${api.login-url}")
    private String url;

    public void sendLoginRequest(String username, String password) {
        ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);

        getCookieFromResponse(getResponse);

        HttpEntity<MultiValueMap<String, String>> request = configureRequest(
                extractLoginToken(Objects.requireNonNull(getResponse.getBody())),
                username,
                password);

        ResponseEntity<String> postResponse = restTemplate.postForEntity(url, request, String.class);

        if (postResponse.getStatusCode() == HttpStatus.SEE_OTHER) {
            getCookieFromResponse(postResponse);
            ResponseEntity<String> redirectedResponse = redirect(Objects.requireNonNull(postResponse.getHeaders()
                                                                                                    .getLocation())
                                                                        .toString());
            System.out.println(redirectedResponse.getBody());
        }
    }

    private String extractLoginToken(String response) {
        if (response.contains("logintoken")) {
            int position = response.indexOf("logintoken") + 19;
            return response.substring(position, position + 32);
        }
        throw new RuntimeException("Couldn't retrieve login token from response.");
    }

    private HttpEntity<MultiValueMap<String, String>> configureRequest(String logintoken, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", sessionConfig.getMoodleSession());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("logintoken", logintoken);
        params.add("username", username);
        params.add("password", password);
        return new HttpEntity<>(params, headers);
    }

    private ResponseEntity<String> redirect(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", sessionConfig.getMoodleSession());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    private void getCookieFromResponse(ResponseEntity<String> response){
        sessionConfig.setMoodleSession(response.getHeaders()
                                                   .getFirst(HttpHeaders.SET_COOKIE));
    }
}
