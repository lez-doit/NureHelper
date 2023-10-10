package com.example.nurehelper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RestService {
    private final RestTemplate restTemplate;
    public String getStringForUrl(String url) {
        return restTemplate.getForObject(url, String.class);
    }
}
