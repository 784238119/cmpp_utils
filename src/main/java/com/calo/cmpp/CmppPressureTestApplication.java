package com.calo.cmpp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CmppPressureTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmppPressureTestApplication.class, args);
    }

}
