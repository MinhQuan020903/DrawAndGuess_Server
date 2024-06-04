package com.backend.socket.friend;

import com.backend.rest.friend.FriendService;
import com.backend.rest.room.RoomService;

import com.backend.rest.user.User;
import com.backend.rest.user.UserService;

import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Configuration
public class UserSocketServerConfig {
    @Autowired
    private SocketIoServer sioServer;
    @Autowired
    private RoomService roomService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private UserService userService;

    private final HashMap<String, String> usernameToSocketId = new HashMap<>();
    @PostConstruct
    public void registerUserNamespace() {
        System.out.println("Creating user socket server.");
        var namespace = sioServer.namespace("/user");
        String userNamespace = "user";
        namespace.on("connection", args -> {
            var socket = (SocketIoSocket) args[0];
            String socketId = socket.getId();
            socket.on("subscribe-user", args1 -> {
                // Get userinfo
                JSONObject obj = (JSONObject) args1[0];
                // Get userinfo
                JSONObject userObj = obj.getJSONObject("user");
                int id = userObj.getInt("id");
                String username = userObj.getString("username");
                User user = userService.findByUsername(username);
                user.setOnline(true);
                userService.save(user);

                usernameToSocketId.put(username, socketId);
                socket.joinRoom(userNamespace);
                System.out.println("User connected: " + username);
                namespace.broadcast(userNamespace, "new-user");
            });

            socket.on("get-users", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String username = obj.getString("username");
                String keyword = obj.getString("keyword");
                ArrayList<String> usernames = new ArrayList<>();
                for (String user : usernameToSocketId.keySet()) {
                    if (friendService.getFriendList(username).stream().noneMatch(u -> u.getUsername().equals(user)) &&
                            !user.equalsIgnoreCase(username) &&
                            user.toLowerCase().contains(keyword.toLowerCase())
                    && !keyword.isEmpty()) {
                        usernames.add(user);
                    }
                }
                socket.send("users", new JSONArray((usernames)));
            });

            socket.on("get-friend-requests", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String username = obj.getString("username");
                var friendRequests = friendService.getFriendRequests(username);
                var friendRequestsReceive = friendService.getFriendRequestsReceive(username);
                List<String> friendRequestsReceiveUsernames = new ArrayList<>();
                for (User user : friendRequestsReceive) {
                    friendRequestsReceiveUsernames.add(user.getUsername());
                }
                socket.send("friend-requests", new JSONArray((friendRequestsReceiveUsernames)));

            });

            socket.on("send-friend-request", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String sender = obj.getString("sender");
                String receiver = obj.getString("receiver");
                try {
                    friendService.addFriendRequest(sender, receiver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                namespace.broadcast(userNamespace, "friend-request", new JSONObject()
                        .put("sender", sender)
                        .put("receiver", receiver));
            });

            socket.on("response-friend-request" ,args1 -> {

                JSONObject obj = (JSONObject) args1[0];
                String sender = obj.getString("sender");
                String receiver = obj.getString("receiver");
                boolean accept = obj.getBoolean("accept");

                if (accept) {
                    System.out.println("Accept friend request" + sender + " " + receiver + " " + accept) ;
                    try {
                        friendService.acceptFriendRequest(sender, receiver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Reject friend request");
                    try {
                        //Delete friend request from sender and receiver
                        User user = userService.findByUsername(receiver);
                        user.removeFriendRequestReceive(sender);
                        userService.save(user);

                        user = userService.findByUsername(sender);
                        user.removeFriendRequest(receiver);
                        userService.save(user);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                namespace.broadcast(userNamespace, "friend-request-response", new JSONObject()
                        .put("sender", sender)
                        .put("receiver", receiver)
                        .put("accept", accept));
            });

            socket.on("get-friends", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String username = obj.getString("username");
                var friends = friendService.getFriendList(username);
                ArrayList<String> friendUsernames = new ArrayList<>();
                for (User user : friends) {
                    if (user.isOnline()) {
                        friendUsernames.add(user.getUsername());
                    }
                }
                socket.send("friends", new JSONArray((friendUsernames)));
            });

            socket.on("invite-friend-to-room", args1 ->{
                JSONObject obj = (JSONObject) args1[0];
                String sender = obj.getString("sender");
                String receiver = obj.getString("receiver");
                Integer roomId = obj.getInt("roomId");
                namespace.broadcast(userNamespace, "invite-friend-to-room", new JSONObject()
                        .put("sender", sender)
                        .put("receiver", receiver)
                        .put("roomId", roomId));
            });

            socket.on("response-invite-friend-to-room", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String sender = obj.getString("sender");
                String receiver = obj.getString("receiver");
                Integer roomId = obj.getInt("roomId");
                boolean accept = obj.getBoolean("accept");
                namespace.broadcast(userNamespace, "response-invite-friend-to-room", new JSONObject()
                        .put("sender", sender)
                        .put("receiver", receiver)
                        .put("roomId", roomId)
                        .put("accept", accept));
            });

            socket.on("disconnect", args1 -> {
                //Get user id based on socketId
                var username = usernameToSocketId.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(socketId))
                        .map(entry -> entry.getKey())
                        .findFirst()
                        .orElse(null);
                User user = userService.findByUsername(username);
                user.setOnline(false);
                userService.save(user);
                usernameToSocketId.values().remove(username);
                socket.disconnect(true);
                System.out.println("User disconnected: " + username);
                namespace.broadcast(userNamespace, "user-disconnected");
            });
        });
    }
}
