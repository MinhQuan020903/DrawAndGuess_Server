package com.backend.socket.draw;

import com.backend.socket.model.DrawMessageModel;
import com.backend.socket.singleton.RoomManager;
import com.backend.utils.JsonUtils;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class DrawSocketServerConfig {

    @Autowired
    private SocketIoServer sioServer;

    @Autowired
    private RoomManager roomManager;

    @PostConstruct
    public void registerRoomNamespace() {
        System.out.println("Creating draw socket server.");
        var namespace = sioServer.namespace("/draw");
        AtomicReference<String> roomId = new AtomicReference<>("");
        AtomicReference<Integer> userId = new AtomicReference<>(0);
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            socket.on("subscribe-room", args1 -> {
                JSONObject obj = (JSONObject) args1[0];

                roomId.set(obj.getString("roomId"));

                JSONObject user = obj.getJSONObject("user");
                int id = user.getInt("id");
                userId.set((id));
                System.out.println("Client " + userId.get() + " is ready at room " + roomId.get() + ", " + socket.getId() + ".");
                socket.joinRoom(roomId.get());
                namespace.broadcast(roomId.get(), "request-canvas-state", JsonUtils.toJsonObj(new DrawMessageModel.User(userId.get(), true, "")));

            });

            socket.on("canvas-state", args1 -> {
                System.out.println("Client has sent canvas state.");
                JSONObject obj = (JSONObject) args1[0];
                String canvasState = obj.getString("canvasState");
                System.out.println("Client has sent canvas state : " + canvasState);
                namespace.broadcast(roomId.get(), "canvas-state-from-server", args1[0]);
            });

            socket.on("draw-line", args1 -> {
                socket.broadcast(roomId.get(), "draw-line", args1[0]);
            });

            socket.on("clear", args1 -> {
                namespace.broadcast(roomId.get(), "clear", "clearing canvas");
            });

            socket.on("disconnect", args1 -> {
                namespace.broadcast(roomId.get(), "disconnect", JsonUtils.toJsonObj(new DrawMessageModel.Message("Client " + userId.get() + " has disconnected.")));
                roomManager.removeUserFromRoom(roomId.get(), String.valueOf(userId.get()));
                socket.disconnect(true);
                System.out.println("Client " + userId.get() + " has disconnected from room.");
            });
        });
    }
}

