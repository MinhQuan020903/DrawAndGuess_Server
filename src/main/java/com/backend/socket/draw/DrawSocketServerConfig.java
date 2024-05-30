package com.backend.socket.draw;

import com.backend.rest.topic.TopicService;
import com.backend.socket.model.Detail;
import com.backend.socket.model.DrawMessageModel;
import com.backend.socket.model.Player;
import com.backend.socket.singleton.RoomManager;
import com.backend.utils.JsonUtils;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class DrawSocketServerConfig {

    // Example of keyword for guessing game
    private String keyword = "apple";
    @Autowired
    private SocketIoServer sioServer;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private TopicService topicService;

    private final Map<String, Timer> gameTimers = new HashMap<>();

    @PostConstruct
    public void registerRoomNamespace() {
        System.out.println("Creating draw socket server.");
        var namespace = sioServer.namespace("/draw");
        AtomicReference<String> roomId = new AtomicReference<>("");
        AtomicReference<Integer> userId = new AtomicReference<>(0);
        AtomicReference<Player> player = new AtomicReference<>(new Player());
        AtomicReference<Player> drawer = new AtomicReference<>(new Player());

        AtomicReference<Integer> countCorrectGuess = new AtomicReference<>(0);


        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            socket.on("subscribe-room", args1 -> {
                JSONObject obj = (JSONObject) args1[0];

                // Get roomId
                roomId.set(obj.getString("roomId"));
                // Get userinfo
                JSONObject user = obj.getJSONObject("user");
                int id = user.getInt("id");
                userId.set((id));
                String username = user.getString("username");

                // Create a player object
                player.set(new Player(id, false, false, 0, new Detail(username)));
                // Add player to room
                if (!roomManager.isPlayerInRoom(roomId.get(), player.get().getId())) {
                    // Join room
                    socket.joinRoom(roomId.get());
                    // Notify the room
                    //JSON object to send to client
                    roomManager.addUserToRoom(roomId.get(), player.get());
                }
                List<Player> roomPlayers = roomManager.getRoom(roomId.get());
                JSONArray roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                namespace.broadcast(roomId.get(), "subscribed", roomPlayersJson);
                System.out.println("Size of room " + roomId.get() + " is " + roomManager.getRoom(roomId.get()).size());
                System.out.println("Client " + player.get().getDetail().getUsername() + " is ready at room " + roomId.get());
            });

            socket.on("start-game", args1 -> {
                // Pick randomly a user from a room to be the drawer
                System.out.println("Starting game in room " + roomId.get() + ", size: " + roomManager.getRoom(roomId.get()).size());
                drawer.set(roomManager.getRandomUserFromRoom(roomId.get()));
                // Get the player list from room
                List<Player> roomPlayers = roomManager.getRoom(roomId.get());
                for (Player p : roomPlayers) {
                    if (Objects.equals(p.getId(), drawer.get().getId())) {
                        p.setCurrentTurn(true);
                        p.setNextTurn(false);
                    } else {
                        p.setCurrentTurn(false);
                        p.setNextTurn(true);
                    }
                }
                System.out.println("Room " + roomId.get() + " is playing!");
                namespace.broadcast(roomId.get(), "server-start-game", JsonUtils.toJsonObj(new DrawMessageModel.User((drawer.get().getId()), true, "")));
                // Send the player list to client
                // Serialize the player list to JSON array
                JSONArray roomPlayersJson;
                try {
                    roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                    System.out.println("Room players JSON: " + roomPlayersJson);
                    namespace.broadcast(roomId.get(), "server-list-players", roomPlayersJson);
                } catch (Exception e) {
                    System.err.println("Failed to serialize room players to JSON: " + e.getMessage());
                    e.printStackTrace();}

            });

            socket.on("end-game", args1 -> {
                Integer drawerScore = Math.round((float) countCorrectGuess.get() * 100 / (roomManager.getRoom(roomId.get()).size() - 1));
                roomManager.getRoom(roomId.get()).forEach(player1 -> {
                    if (player1.getId() == drawer.get().getId()){
                        player1.setPoints(player1.getPoints() + drawerScore);
                    }
                });

                namespace.broadcast(roomId.get(), "drawer-score", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(drawer.get().getId(),"", "", false, drawerScore)));
                countCorrectGuess.set(0);
            });

            socket.on("canvas-state", args1 -> {
                System.out.println("Client has sent canvas state.");
                JSONObject obj = (JSONObject) args1[0];
                String canvasState = obj.getString("canvasState");
                System.out.println("Client has sent canvas state: " + canvasState);
                namespace.broadcast(roomId.get(), "canvas-state-from-server", args1[0]);
            });

            socket.on("draw-line", args1 -> {
                namespace.broadcast(roomId.get(), "request-canvas-state", JsonUtils.toJsonObj(new DrawMessageModel.User(userId.get(), true, "")));
                socket.broadcast(roomId.get(), "draw-line", args1[0]);
            });

            socket.on("clear", args1 -> {
                namespace.broadcast(roomId.get(), "clear", "clearing canvas");
            });

            socket.on("get-keyword", args1 -> {
                socket.send("keyword", JsonUtils.toJsonObj(new DrawMessageModel.Message(keyword)));
            });

            socket.on("send-guess", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String guess = obj.getString("guess");
                Integer id = obj.getInt("id");
                String username = obj.getString("username");
                Integer guessPoint = obj.getInt("guessPoint");

                if (guess.equalsIgnoreCase(keyword)) {
                    roomManager.getRoom(roomId.get()).forEach(player1 -> {
                        if (player1.getId() == id) {
                            player1.setPoints(player1.getPoints() + guessPoint);
                        }
                    });
                    countCorrectGuess.set(countCorrectGuess.get() + 1);
                    namespace.broadcast(roomId.get(), "validate-guess", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(id,"User " + username + " has guessed the word correctly!", guess,true, guessPoint)));
                } else {
                    namespace.broadcast(roomId.get(), "validate-guess", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(id,"User " + username + " has guessed the word: ", guess, false, 0)));
                }
            });

            socket.on("disconnect", args1 -> {
                if (roomManager.getRoom(roomId.get()).size() == 1) {
                    roomManager.removeLastUserFromRoom(roomId.get());
                } else {
                    roomManager.removeUserFromRoomWithId(roomId.get(), (userId.get()));
                    List<Player> roomPlayers = roomManager.getRoom(roomId.get());
                    JSONArray roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                    namespace.broadcast(roomId.get(), "player-disconnect", roomPlayersJson);
                    System.out.println("Client " + userId.get() + " has disconnected from room.");
                }
            });
        });
    }

}
