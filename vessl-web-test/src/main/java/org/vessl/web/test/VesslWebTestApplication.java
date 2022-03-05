package org.vessl.web.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.vessl"})

public class VesslWebTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesslWebTestApplication.class, args);
    }

}
