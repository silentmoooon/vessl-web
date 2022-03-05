package org.vessl.web.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebService implements WebServiceInterface {
    @Value("${name}")
    private String name;
    @Override
    public String getName() {
        return name;
    }
}
