package com.backend.socket.singleton;

import com.backend.socket.model.Player;
import com.backend.utils.HashUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Getter
@Component
public class RoomManager {
    public static final int ROOM_CAPACITY = 10;
    public static final int NUMBER_OF_ROOMS = 10;
    private final ConcurrentHashMap<String, List<Player>> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();


    public RoomManager() {
        IntStream.rangeClosed(1, NUMBER_OF_ROOMS).forEach(i -> {
            String roomName = "room" + i;
            String hashedRoomName = HashUtils.hashRoomName(roomName);
            rooms.put(hashedRoomName, new ArrayList<>());
        });

        for (var entry : rooms.entrySet()) {
            System.out.println("Room: " + entry.getKey() + " has " + entry.getValue().size() + " clients.");
        }
    }

    public List<Player> getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void addUserToRoom(String roomId, Player user) {
        rooms.get(roomId).add(user);
    }

    public boolean isPlayerInRoom(String roomId, int userId) {
        return rooms.get(roomId).stream().anyMatch(user -> user.getId() == userId);
    }

    public void removeUserFromRoom(String roomId, Player user) {
        rooms.get(roomId).remove(user);
    }

    public void removeUserFromRoomWithId(String roomId, int userId) {
        rooms.get(roomId).removeIf(user -> user.getId() == userId);
    }

    public void removeLastUserFromRoom(String roomId) {
        rooms.get(roomId).removeFirst();
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public boolean isRoomFull(String roomId) {
        return rooms.get(roomId).size() >= ROOM_CAPACITY;
    }

    public boolean addRoom(String roomId) {
        if (rooms.containsKey(roomId)) {
            return false; // Room already exists
        }
        rooms.put(roomId, new ArrayList<>());
        System.out.println("Added new room: " + roomId);
        return true;
    }

    public boolean removeRoom(String roomId) {
        if (!rooms.containsKey(roomId)) {
            return false; // Room does not exist
        }
        rooms.remove(roomId);
        System.out.println("Removed room: " + roomId);
        return true;
    }

    public Player getRandomUserFromRoom(String roomId) {
        List<Player> room = rooms.get(roomId);
        if (room == null || room.isEmpty()) {
            return null; // Room does not exist or is empty
        }
        int randomIndex = random.nextInt(room.size());
        return room.get(randomIndex);
    }
}
