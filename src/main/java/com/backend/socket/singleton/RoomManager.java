package com.backend.socket.singleton;

import com.backend.utils.HashUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Getter
@Component
public class RoomManager {
    public static final int ROOM_CAPACITY = 10;
    public static final int NUMBER_OF_ROOMS = 10;
    private final ConcurrentHashMap<String, List<String>> rooms = new ConcurrentHashMap<>();

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

    public List<String> getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void addUserToRoom(String roomId, String userId) {
        rooms.get(roomId).add(userId);
    }

    public void removeUserFromRoom(String roomId, String userId) {
        rooms.get(roomId).remove(userId);
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
}
