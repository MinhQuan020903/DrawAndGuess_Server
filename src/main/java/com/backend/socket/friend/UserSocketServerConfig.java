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

                usernameToSocketId.put(socketId, username);
                socket.joinRoom(userNamespace);
                System.out.println("User connected: " + Arrays.toString(usernameToSocketId.values().toArray()));
                namespace.broadcast(userNamespace, "new-user");
            });

            socket.on("get-users", args1 -> {
                JSONObject obj = (JSONObject) args1[0];
                String username = obj.getString("username");
                ArrayList<String> usernames = new ArrayList<>();
                for (String user : usernameToSocketId.values()) {
                    if (friendService.getFriendList(username).stream().noneMatch(u -> u.getUsername().equals(user))) {
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
                System.out.println("sender:"  + userService.findByUsername(sender).getFriendRequests());
                System.out.println("receiver:"  + userService.findByUsername(receiver).getFriendRequestsReceive());
                if (accept) {
                    try {
                        friendService.acceptFriendRequest(sender, receiver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                userService.findByUsername(receiver).removeFriendRequest(sender);
                userService.findByUsername(sender).removeFriendRequestReceive(receiver);
                System.out.println("aloha" + friendService.getFriendList(sender));
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
                System.out.println("friends of user: " + username  + ": " + friendUsernames);
                socket.send("friends", new JSONArray((friendUsernames)));
            });

            socket.on("disconnect", args1 -> {
                //Get user id based on socketId
                var username = usernameToSocketId.get(socketId);// Remove userId from map on disconnect
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
