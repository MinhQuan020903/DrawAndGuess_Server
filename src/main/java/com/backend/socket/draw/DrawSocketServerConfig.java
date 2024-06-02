package com.backend.socket.draw;

import com.backend.rest.room.RoomService;
import com.backend.rest.topic.Topic;
import com.backend.rest.topic.TopicService;
import com.backend.socket.model.Detail;
import com.backend.socket.model.DrawMessageModel;
import com.backend.socket.model.Player;
import com.backend.socket.model.PlayerSocketInfo;
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

    @Autowired
    private SocketIoServer sioServer;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private TopicService topicService;

    private final Random random = new Random();

    private final HashMap<String, PlayerSocketInfo> socketIdToRoomId = new HashMap<>();

    @PostConstruct
    public void registerRoomNamespace() {
        System.out.println("Creating draw socket server.");
        var namespace = sioServer.namespace("/draw");


        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            socket.on("subscribe-room", args1 -> {
                JSONObject obj = (JSONObject) args1[0];

                // Get roomId
                Integer roomId = obj.getInt("roomId");
                // Get userinfo
                JSONObject user = obj.getJSONObject("user");

                int id = user.getInt("id");

                String username = user.getString("username");

                // Create a player object
                Player player = new Player(id, false, false, 0, new Detail(username));
                // Add player to room
                if (!roomManager.isPlayerInRoom(roomId, id)) {
                    // Join room
                    socket.joinRoom(String.valueOf(roomId));
                    try {
                        roomManager.addUserToRoom(roomId, player, socket.getId());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    socketIdToRoomId.put(socket.getId(), new PlayerSocketInfo(roomId, id));
//                    System.out.println("Added socket:" + socket.getId() + " to room " + roomId);
                }
                List<Player> roomPlayers = roomManager.getRoomPlayersById(roomId);
                JSONArray roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                namespace.broadcast(String.valueOf(roomId), "subscribed", roomPlayersJson);
//                System.out.println("Size of room " + roomId + " is " + roomManager.getRoomPlayersById(roomId).size());
//                System.out.println("Client " + player.getDetail().getUsername() + " is ready at room " + roomId);
            });

            socket.on("start-game", args1 -> {
                JSONObject obj = (JSONObject) args1[0];

                Integer roomId = obj.getInt("roomId");
                // Check if this is a new game
                //If true, reset all players' points
                if (obj.getBoolean("newGame")) {
                    roomManager.getRoomPlayersById(roomId).forEach(player1 -> player1.setPoints(0));
                }
                // Pick randomly a user from a room to be the drawer
                System.out.println("Starting game in room " + roomId + ", size: " + roomManager.getRoomPlayersById(roomId).size());
                Player drawer = roomManager.getRandomUserFromRoom(roomId);
                roomManager.getRoomDetailById(roomId).setDrawer(drawer);
                // Get the player list from room
                List<Player> roomPlayers = roomManager.getRoomPlayersById(roomId);
                for (Player p : roomPlayers) {
                    if (Objects.equals(p.getId(), drawer.getId())) {
                        p.setCurrentTurn(true);
                        p.setNextTurn(false);
                    } else {
                        p.setCurrentTurn(false);
                        p.setNextTurn(true);
                    }
                }
                namespace.broadcast(String.valueOf(roomId), "server-start-game", JsonUtils.toJsonObj(new DrawMessageModel.User((drawer.getId()), true, "")));
                // Send the player list to client
                // Serialize the player list to JSON array
                JSONArray roomPlayersJson;
                try {
                    roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                    namespace.broadcast(String.valueOf(roomId), "server-list-players", roomPlayersJson);
                } catch (Exception e) {
                    System.err.println("Failed to serialize room players to JSON: " + e.getMessage());
                    e.printStackTrace();}

            });

            socket.on("draw-line", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer roomId = obj.getInt("roomId");
                socket.broadcast(String.valueOf(roomId), "draw-line", args1[0]);
            });

            socket.on("clear", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer roomId = obj.getInt("roomId");
                namespace.broadcast(String.valueOf(roomId), "clear", "clearing canvas");
            });

            socket.on("get-room-detail", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer roomId = obj.getInt("roomId");
                namespace.broadcast(String.valueOf(roomId), "room-detail", JsonUtils.toJsonObj(roomManager.getRoomById(roomId)));
            });

            socket.on("get-keyword", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer roomId = obj.getInt("roomId");
                try {
                    Topic topic = topicService.getTopicById(roomManager.getRoomById(roomId).getTopicId());
                    String[] words = topic.getWords();
                    roomManager.getRoomDetailById(roomId).setKeyword(words[random.nextInt(words.length)]);
                } catch (Exception e) {
                    System.err.println("Failed to get keyword: " + e.getMessage());
                    e.printStackTrace();
                }
                socket.send("keyword", JsonUtils.toJsonObj(new DrawMessageModel.Message(roomManager.getRoomDetailById(roomId).getKeyword())));
            });

            socket.on("send-guess", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer roomId = obj.getInt("roomId");
                String guess = obj.getString("guess");
                Integer id = obj.getInt("id");
                String username = obj.getString("username");
                Integer guessPoint = obj.getInt("guessPoint");

                String keyword = roomManager.getRoomDetailById(roomId).getKeyword();

                if (guess.equalsIgnoreCase(keyword)) {
                    roomManager.getRoomPlayersById(roomId).forEach(player1 -> {
                        if (player1.getId() == id) {
                            player1.setPoints(player1.getPoints() + guessPoint);
                        }
                    });
                    roomManager.getRoomDetailById(roomId).setCountCorrectGuess(
                            roomManager.getRoomDetailById(roomId).getCountCorrectGuess() + 1
                    );
                    namespace.broadcast(String.valueOf(roomId), "validate-guess", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(id,"User " + username + " has guessed the word correctly!", guess,true, guessPoint)));
                } else {
                    namespace.broadcast(String.valueOf(roomId), "validate-guess", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(id,"User " + username + " has guessed the word: ", guess, false, 0)));
                }
            });

            socket.on("end-game", args1 -> {
                try {
                    JSONObject obj = (JSONObject) args1[0];
                    Integer roomId = obj.getInt("roomId");
                    Integer countCorrectGuess = roomManager.getRoomDetailById(roomId).getCountCorrectGuess();
                    Integer drawerScore = Math.round((float) countCorrectGuess * 100 / (roomManager.getRoomPlayersById(roomId).size() - 1));
                    Player drawer = roomManager.getRoomDetailById(roomId).getDrawer();
                    roomManager.getRoomPlayersById(roomId).forEach(player1 -> {
                        if (player1.getId() == drawer.getId()){
                            player1.setPoints(player1.getPoints() + drawerScore);
                        }
                    });

                    namespace.broadcast(String.valueOf(roomId), "drawer-score", JsonUtils.toJsonObj(new DrawMessageModel.GuessMessage(drawer.getId(),"", "", false, drawerScore)));
                    roomManager.getRoomDetailById(roomId).setCountCorrectGuess(0);

                    //Check if there are any players with score >= maxScore
                    List<Player> roomPlayers = roomManager.getRoomPlayersById(roomId);
                    if (roomPlayers.stream().anyMatch(player1 -> player1.getPoints() >= roomManager.getRoomById(roomId).getMaxScore())) {

                        Player winner = roomPlayers.getFirst();
                        for (Player p : roomPlayers) {
                            if (p.getPoints() > winner.getPoints() && p.getPoints() >= roomManager.getRoomById(roomId).getMaxScore()) {
                                winner = p;
                            }
                        }
                        System.out.println("Found a winner:" + winner);
                        //Reset all players' points
//                    roomPlayers.forEach(player1 -> player1.setPoints(0));
                        //Send the winner to all clients
                        namespace.broadcast(String.valueOf(roomId), "found-winner", JsonUtils.toJsonObj(winner));
                    }
                } catch  (Exception e) {
                    System.err.println("Failed to end game: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            socket.on("exit-room", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                Integer id = obj.getInt("id");
                Integer roomId = obj.getInt("roomId");
                String username = obj.getString("username");
                try {
                    roomManager.removeUserFromRoom(roomId, username);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                List<Player> roomPlayers = roomManager.getRoomPlayersById(roomId);
                JSONArray roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                namespace.broadcast(String.valueOf(roomId), "player-disconnect", roomPlayersJson);
                System.out.println("Client " + username + " has disconnected from room.");

            });



            socket.on("disconnect", args1 -> {
                roomManager.getRooms().forEach((room, roomDetail) -> {
                    Integer userId = socketIdToRoomId.get(socket.getId()).getUserId();
                    Integer roomId = socketIdToRoomId.get(socket.getId()).getRoomId();
                    String username = roomManager.getRoomPlayersById(roomId).stream().filter(player -> player.getId() == userId).findFirst().get().getDetail().getUsername();
                    try {
                        roomManager.removeUserFromRoom(roomId, username);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    List<Player> roomPlayers = roomManager.getRoomPlayersById(roomId);
                    JSONArray roomPlayersJson = JsonUtils.toJsonArray(roomPlayers);
                    namespace.broadcast(String.valueOf(roomId), "player-disconnect", roomPlayersJson);
                    System.out.println("Client " + username + " has disconnected from room.");
                });
            });
        });
    }

}
