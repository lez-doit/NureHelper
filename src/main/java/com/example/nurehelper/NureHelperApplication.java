package com.example.nurehelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NureHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(NureHelperApplication.class, args);
    }

}
