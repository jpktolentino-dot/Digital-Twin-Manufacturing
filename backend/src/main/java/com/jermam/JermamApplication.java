package com.jermam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JermamApplication {
    public static void main(String[] args) {
        SpringApplication.run(JermamApplication.class, args);
    }
}
