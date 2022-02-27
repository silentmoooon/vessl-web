package org.vessl.webstarter;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vessl")
@Data
@ToString
public class VesslProperties {

    private Server server = new Server();

    @Data
    public static class Server {
        private int port = 8000;
    }
}
