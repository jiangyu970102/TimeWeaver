package com.timeweaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeWeaverApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeWeaverApplication.class, args);
    }
}
