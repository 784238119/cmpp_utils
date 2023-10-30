package com.calo.cmpp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CmppUtilApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmppUtilApplication.class, args);
    }

}
