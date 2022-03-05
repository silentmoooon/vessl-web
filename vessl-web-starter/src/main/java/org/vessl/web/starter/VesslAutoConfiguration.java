package org.vessl.web.starter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vessl.web.handle.WebServer;

@EnableConfigurationProperties(value = VesslProperties.class)
@Configuration
public class VesslAutoConfiguration {
    @Bean
    public WebServer webServer(VesslProperties vesslProperties){
        WebServer webServer = new WebServer();
        webServer.setPort(vesslProperties.getServer().getPort());
        return webServer;
    }
}
