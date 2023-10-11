package com.example.nurehelper.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class LoginDTO implements Serializable {
    private String loginToken;
    private String cookie;

    private String login;
    private String password;
}
