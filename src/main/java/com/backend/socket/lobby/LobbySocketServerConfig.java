package com.backend.socket.lobby;

import com.backend.utils.HashUtils;
import com.backend.utils.JsonUtils;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Configuration
public class LobbySocketServerConfig {
    @Autowired
    private SocketIoServer sioServer;
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


    @PostConstruct
    public void registerLobbyNamespace() {
        System.out.println("Creating lobby socket server.");
        var namespace = sioServer.namespace("/lobby");
        AtomicReference<String> roomId = new AtomicReference<>("");
        AtomicReference<Integer> userId = new AtomicReference<>(0);
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            socket.on("subscribe-lobby", args1 -> {

                JSONObject userObj = (JSONObject) args1[0];

                JSONObject user = userObj.getJSONObject("user");
                // Parse the user details
                int id = user.getInt("id");
                String username = user.getString("username");

                userId.set(id);

                System.out.println("Client " + userId.get() + ", " + username + " subscribed to lobby.");
                var roomsList = new ArrayList<JSONObject>();
                rooms.forEach((room_id, userList) -> {
                    var roomObj = new JSONObject();
                    roomObj.put("id", room_id);
                    roomObj.put("capacity", ROOM_CAPACITY);
                    roomObj.put("currentCapacity", userList.size());
                    roomsList.add(roomObj);
                });
                socket.send("rooms-list", roomsList.toString());
            });

            socket.on("join-room", args1 -> {

                JSONObject obj = (JSONObject) args1[0];

                String room_id = obj.getString("roomId");
                roomId.set(room_id);

                JSONObject user = obj.getJSONObject("user");
                // Parse the user details
                int id = user.getInt("id");

                if (rooms.containsKey(roomId.get()) && rooms.get(roomId.get()).size() < ROOM_CAPACITY && !rooms.get(roomId.get()).contains(String.valueOf(id))) {
                    rooms.get(roomId.get()).add(String.valueOf(id));
                    socket.send("room-joined", roomId.get());
                } else {
                    socket.send("room-full", roomId.get());
                }
            });

            socket.on("disconnect", args1 -> {
                namespace.broadcast(roomId.get(), "disconnect", JsonUtils.toJsonObj(new DrawMessageModel.Message("Client " + userId.get() + " has disconnected.")));
                socket.disconnect(true);
                System.out.println("Client " + userId.get() + " has disconnected from lobby.");
            });
        });
    }

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
                // Parse the user details
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
                rooms.get(roomId.get()).remove(String.valueOf(userId.get()));
                socket.disconnect(true);
                System.out.println("Client " + userId.get() + " has disconnected from room.");
            });
        });
    }
}
