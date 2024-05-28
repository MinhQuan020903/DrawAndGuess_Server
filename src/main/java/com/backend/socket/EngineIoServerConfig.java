package com.backend.socket;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineIoServerConfig {

    @Bean
    public EngineIoServer engineIoServer() {
        var options = EngineIoServerOptions.newFromDefault();
        options.setCorsHandlingDisabled(true);
        return new EngineIoServer(options);
    }
}
