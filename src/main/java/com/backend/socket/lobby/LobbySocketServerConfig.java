package com.backend.socket.lobby;

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
public class LobbySocketServerConfig {

    @Autowired
    private SocketIoServer sioServer;

    @Autowired
    private RoomManager roomManager;

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
                int id = user.getInt("id");
                String username = user.getString("username");

                userId.set(id);

                System.out.println("Client " + userId.get() + ", " + username + " subscribed to lobby.");
                var roomsList = new ArrayList<JSONObject>();
                roomManager.getRooms().forEach((room_id, userList) -> {
                    var roomObj = new JSONObject();
                    roomObj.put("id", room_id);
                    roomObj.put("capacity", RoomManager.ROOM_CAPACITY);
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
                int id = user.getInt("id");

                if (roomManager.roomExists(roomId.get()) && !roomManager.isRoomFull(roomId.get()) && !roomManager.getRoom(roomId.get()).contains(String.valueOf(id))) {
                    socket.send("room-joined", roomId.get());
                } else {
                    socket.send("room-full", roomId.get());
                }
            });

            socket.on("disconnect", args1 -> {
                sioServer.namespace("/lobby").broadcast(roomId.get(), "disconnect", JsonUtils.toJsonObj(new DrawMessageModel.Message("Client " + userId.get() + " has disconnected.")));
                socket.disconnect(true);
                System.out.println("Client " + userId.get() + " has disconnected from lobby.");
            });
        });
    }
}
