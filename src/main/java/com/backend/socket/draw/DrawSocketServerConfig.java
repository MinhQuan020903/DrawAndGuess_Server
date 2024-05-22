package com.backend.socket.draw;


import com.backend.utils.JsonUtils;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoSocket;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import io.socket.engineio.server.EngineIoServer;
import io.socket.socketio.server.SocketIoServer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicReference;

record Message(
        String message) {

    String getMessage() {
        return message;
    }
};
record User(
        String userId,
        boolean isPlayer,
        Object data) {
    @Override
    public String userId() {
        return userId;
    }

    @Override
    public boolean isPlayer() {
        return isPlayer;
    }

    @Override
    public Object data() {
        return data;
    }
}

@Configuration
public class DrawSocketServerConfig {

    @Bean
    EngineIoServer engineIoServer() {
        var opt = EngineIoServerOptions.newFromDefault();
        opt.setCorsHandlingDisabled(true);
        var eioServer = new EngineIoServer(opt);
        return eioServer;
    }

    @Bean
    SocketIoServer socketIoServer(EngineIoServer eioServer) {
        var sioServer = new SocketIoServer(eioServer);

        // Create a namespace for the draw socket server
        var namespace = sioServer.namespace("/draw");
        // Add a connection listener
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            System.out.println("Client " + socket.getId() + " has connected.");

            String room = "room1";
            socket.joinRoom(room);

            socket.on("client-ready", args1 -> {
                System.out.println("Client " + socket.getId() + " is ready.");
                namespace.broadcast(room, "request-canvas-state", JsonUtils.toJsonObj(new User("1", true, ""))
            );
            });
            //Send initial canvas state to the client
            //Set the first client to be the drawer
            // Add a listener for the "canvas-state" event
            socket.on("canvas-state", args1 -> {
                System.out.println("Client " + socket.getId() + " has sent canvas state.");
                namespace.broadcast(room,"canvas-state-from-server", args1[0]);
            });

            // Add a listener for the "draw-line" event
            socket.on("draw-line", args1 -> {
                socket.broadcast(room,"draw-line", args1[0]);
            });

            // Add a listener for the "clear" event
            socket.on("clear", args1 -> {
                namespace.broadcast(room,"clear", String.valueOf("clearing canvas"));
            });

            // Add a disconnect listener
            socket.on("disconnect", args1 -> {
                namespace.broadcast(room,"disconnect", JsonUtils.toJsonObj(new Message("Client " + socket.getId() + " has disconnected.")));
                socket.disconnect(true);
                System.out.println("Client " + socket.getId() + " has disconnected.");
            });
        });

        return sioServer;
    }
}
