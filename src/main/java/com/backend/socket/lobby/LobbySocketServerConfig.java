package com.backend.socket.lobby;

import com.backend.rest.room.entity.Room;
import com.backend.rest.topic.TopicService;
import com.backend.socket.model.DrawMessageModel;
import com.backend.socket.singleton.RoomManager;
import com.backend.utils.JsonUtils;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class LobbySocketServerConfig {

    @Autowired
    private SocketIoServer sioServer;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private TopicService topicService;

    private ArrayList<JSONObject> getRoomList() {
        ArrayList<JSONObject>  roomsList = new ArrayList<JSONObject>();
        roomManager.getRooms().forEach((room, userList) -> {
            if (room.isPublic()) {
                var roomObj = new JSONObject();
                roomObj.put("id", room.getRoomId());
                roomObj.put("capacity", room.getCapacity());
                roomObj.put("currentCapacity", userList.size());
                roomObj.put("topic", topicService.getTopicById(room.getTopicId()).getName());
                roomsList.add(roomObj);
            }
        });
        roomsList.sort((o1, o2) -> o1.getInt("id") - o2.getInt("id"));
        return roomsList;
    }

    @PostConstruct
    public void registerLobbyNamespace() {
        System.out.println("Creating lobby socket server.");
        var namespace = sioServer.namespace("/lobby");
        AtomicReference<Integer> roomId = new AtomicReference<>(1);
        AtomicReference<Integer> userId = new AtomicReference<>(0);
        String lobbyNamespace = "lobby";
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            socket.on("subscribe-lobby", args1 -> {
                //Get userinfo
                JSONObject userObj = (JSONObject) args1[0];
                JSONObject user = userObj.getJSONObject("user");
                int id = user.getInt("id");
                String username = user.getString("username");
                userId.set(id);

                socket.joinRoom(lobbyNamespace);
                //Get roomlist to show in lobby
                System.out.println("Client " + userId.get() + ", " + username + " subscribed to lobby.");

                namespace.broadcast( lobbyNamespace,"rooms-list", getRoomList().toString());
            });

            socket.on("create-room", args1 ->{
                JSONObject obj = (JSONObject) args1[0];
                int topicId = obj.getInt("topicId");
                int capacity = obj.getInt("capacity");
                int maxScore = obj.getInt("maxScore");
                boolean isPublic = obj.getBoolean("isPublic");
                String username = obj.getString("username");

                Room createdRoom = roomManager.createRoom(topicId, capacity, maxScore, isPublic, username);
                socket.send("room-created", createdRoom.getRoomId());
                namespace.broadcast(lobbyNamespace, "rooms-list", getRoomList().toString());
            });

            socket.on("join-room", args1 -> {

                //Get room info that user wants to join
                JSONObject obj = (JSONObject) args1[0];

                int room_id = obj.getInt("roomId");
                roomId.set(room_id);

                //Get userinfo
                JSONObject user = obj.getJSONObject("user");
                int id = user.getInt("id");
                userId.set(id);

                if (roomManager.roomExists(roomId.get()) && !roomManager.isRoomFull(roomId.get()) && !roomManager.isPlayerInRoom(roomId.get(), userId.get())) {
                    socket.send("room-joined", roomId.get());
                } else {
                    socket.send("room-full", roomId.get());
                }
            });

            socket.on("disconnect", args1 -> {
                socket.disconnect(true);
                namespace.broadcast(lobbyNamespace, "rooms-list", getRoomList().toString());
                namespace.broadcast(lobbyNamespace, "disconnect", JsonUtils.toJsonObj(new DrawMessageModel.Message("Client " + userId.get() + " has disconnected.")));
                System.out.println("Client " + userId.get() + " has disconnected from lobby.");
            });
        });
    }
}
