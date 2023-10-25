package com.example.nurehelper;

import com.example.nurehelper.config.SessionConfig;
import com.example.nurehelper.model.TimetableUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NureApiService {
    private final RestTemplate restTemplate;
    private final SessionConfig sessionConfig;

    @Value("${api.login-url}")
    private String loginUrl;

    @Value("${api.timetable-url}")
    private String timetableUrl;

    public void sendLoginRequest(String username, String password) {
        ResponseEntity<String> getResponse = restTemplate.getForEntity(loginUrl, String.class);

        getCookieFromResponse(getResponse);

        HttpEntity<MultiValueMap<String, String>> request = configureRequest(
                extractLoginToken(Objects.requireNonNull(getResponse.getBody())),
                username,
                password);

        ResponseEntity<String> postResponse = restTemplate.postForEntity(loginUrl, request, String.class);

        if (postResponse.getStatusCode() == HttpStatus.SEE_OTHER) {
            getCookieFromResponse(postResponse);
            ResponseEntity<String> redirectedResponse = sendGetRequestWithCookie(Objects.requireNonNull(postResponse.getHeaders()
                                                                                                                    .getLocation())
                                                                                        .toString());
            //System.out.println(redirectedResponse.getBody());
        }
    }

    public void getTimetable() {
        ResponseEntity<String> getResponse = sendGetRequestWithCookie(timetableUrl);
        List<TimetableUnit> timetable = extractTimetable(getResponse.getBody());
    }

    private String extractLoginToken(String response) {
        if (response.contains("logintoken")) {
            int position = response.indexOf("logintoken") + 19;
            return response.substring(position, position + 32);
        }
        throw new RuntimeException("Couldn't retrieve login token from response.");
    }

    private List<TimetableUnit> extractTimetable(String response) {
        String activityFlag = "Відвідування";
        String activityEndFlag = "Перейти до активності";
        List<TimetableUnit> result = new ArrayList<>();

        while (response.contains(activityFlag)) {
            List<String> activityLines = extractActivityLines(response, activityFlag, activityEndFlag);

            TimetableUnit unit = new TimetableUnit(extractName(activityLines),
                    extractLink(activityLines),
                    extractTimeStart(activityLines),
                    extractTimeEnd(activityLines));
            result.add(unit);
            response = response.substring(response.indexOf(activityEndFlag) + activityEndFlag.length());
        }

        return result;
    }

    private LocalTime extractTimeEnd(List<String> activityLines) {
        String flag = "Коли";
        String timeLine = getLineByFlag(activityLines, flag);
        int timeEndIndex = timeLine.indexOf("/strong") + 9;
        return LocalTime.parse(timeLine.substring(timeEndIndex, timeEndIndex + 5));
    }

    private LocalTime extractTimeStart(List<String> activityLines) {
        String flag = "Коли";
        String timeLine = getLineByFlag(activityLines, flag);
        int timeStartIndex = timeLine.indexOf("/a") + 5;
        return LocalTime.parse(timeLine.substring(timeStartIndex, timeStartIndex + 5));
    }

    private String extractName(List<String> activityLines) {
        String flag = "Курс";
        String nameLine = getLineByFlag(activityLines, flag);
        nameLine = nameLine.substring(nameLine.indexOf("href"));

        int nameStartIndex = nameLine.indexOf(">") + 1;
        int nameEndIndex = nameLine.indexOf("</a>");
        return nameLine
                .substring(nameStartIndex, nameEndIndex);
    }

    private String extractLink(List<String> activityLines) {
        String linkLine = activityLines.get(activityLines.size() - 1);

        int linkStartIndex = linkLine.indexOf("href") + 6;
        int linkEndIndex = linkLine.indexOf("class") - 2;

        return linkLine.substring(linkStartIndex, linkEndIndex);
    }

    private List<String> extractActivityLines(String response, String activityFlag, String activityEndFlag) {
        int activityFlagPosition = response.indexOf(activityFlag);
        int activityEndFlagPosition = response.indexOf(activityEndFlag);
        List<String> linesAfterFlag = Arrays.stream(response.substring(activityFlagPosition, activityEndFlagPosition)
                                                            .split("\n"))
                                            .toList();
        for (String line : linesAfterFlag) {
            System.out.println(line);
        }
        return linesAfterFlag;
    }

    private String getLineByFlag(List<String> activityLines, String flag) {
        String result = "";
        for (int i = 0; i < activityLines.size(); i++) {
            if (activityLines.get(i)
                             .contains(flag)) {
                result = activityLines.get(i + 1);
            }
        }
        return result;
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

    private ResponseEntity<String> sendGetRequestWithCookie(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", sessionConfig.getMoodleSession());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    private void getCookieFromResponse(ResponseEntity<String> response) {
        sessionConfig.setMoodleSession(response.getHeaders()
                                               .getFirst(HttpHeaders.SET_COOKIE));
    }

}
