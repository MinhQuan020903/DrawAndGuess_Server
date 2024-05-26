package com.backend.socket;

import io.socket.engineio.server.EngineIoServer;
import io.socket.socketio.server.SocketIoServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketServerConfig {

    @Bean
    public SocketIoServer socketIoServer(EngineIoServer eioServer) {
        return new SocketIoServer(eioServer);
    }
}