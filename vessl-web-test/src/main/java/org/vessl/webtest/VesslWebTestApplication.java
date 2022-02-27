package org.vessl.webtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.vessl"})

public class VesslWebTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesslWebTestApplication.class, args);
    }

}
