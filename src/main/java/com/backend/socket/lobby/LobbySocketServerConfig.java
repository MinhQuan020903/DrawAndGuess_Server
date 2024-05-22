package com.backend.socket.lobby;

import com.backend.socket.draw.DrawMessageModel;
import com.backend.utils.HashUtils;
import com.backend.utils.JsonUtils;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class LobbySocketServerConfig {
    private static final int ROOM_CAPACITY = 10;
    private static final int NUMBER_OF_ROOMS = 10;

    private final ConcurrentHashMap<String, List<String>> rooms = new ConcurrentHashMap<>();

    // Initialize the rooms with hashed names
    public LobbySocketServerConfig() {
        IntStream.rangeClosed(1, NUMBER_OF_ROOMS).forEach(i -> {
            String roomName = "room" + i;
            String hashedRoomName = HashUtils.hashRoomName(roomName);
            rooms.put(hashedRoomName, new ArrayList<>());
        });

        for (var entry : rooms.entrySet()) {
            System.out.println("Room: " + entry.getKey() + " has " + entry.getValue().size() + " clients.");
        }
    }

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

        var namespace = sioServer.namespace("/draw");
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            System.out.println("Client " + socket.getId() + " has connected.");

            socket.on("subscribe-lobby", args1 -> {
                System.out.println("Client " + socket.getId() + " subscribed to lobby.");
                var roomsList = new ArrayList<JSONObject>();
                rooms.forEach((roomId, userList) -> {
                    var roomObj = new JSONObject();
                    roomObj.put("id", roomId);
                    roomObj.put("capacity", ROOM_CAPACITY);
                    roomObj.put("currentCapacity", userList.size());
                    roomsList.add(roomObj);
                });
                socket.send("rooms-list", roomsList.toString());
            });

            socket.on("join-room", args1 -> {
                String roomId = (String) args1[0];
                if (rooms.containsKey(roomId) && rooms.get(roomId).size() < ROOM_CAPACITY) {
                    rooms.get(roomId).add(socket.getId());
                    socket.joinRoom(roomId);
                    System.out.println("Client " + socket.getId() + " joined " + roomId);
                    socket.send("room-joined", roomId);
                } else {
                    socket.send("room-full", roomId);
                }
            });

            socket.on("disconnect", args1 -> {
                rooms.forEach((roomId, userList) -> userList.remove(socket.getId()));
                System.out.println("Client " + socket.getId() + " has disconnected.");
            });
        });

        return sioServer;
    }
}
