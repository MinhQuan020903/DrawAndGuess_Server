package com.backend.socket.singleton;

import com.backend.rest.room.RoomService;
import com.backend.rest.room.dto.MakeRoomRequest;
import com.backend.rest.room.entity.Room;
import com.backend.rest.topic.TopicService;
import com.backend.socket.model.Player;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Getter
@Component
public class RoomManager {
    public static final int ROOM_CAPACITY = 10;
    public static final int NUMBER_OF_ROOMS = 10;
    private final ConcurrentHashMap<Room, List<Player>> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private final RoomService roomService;

    @Autowired
    private final TopicService topicService;


    public RoomManager(RoomService roomService, TopicService topicService) {

        this.roomService = roomService;
        this.topicService = topicService;
        //Create room
        IntStream.rangeClosed(1, NUMBER_OF_ROOMS).forEach(i -> {
//            String roomName = "room" + i;
//            String hashedRoomName = HashUtils.hashRoomName(roomName);
            MakeRoomRequest makeRoomRequest = new MakeRoomRequest();
            makeRoomRequest.setCapacity(ROOM_CAPACITY);
            makeRoomRequest.setPublic(true);
            makeRoomRequest.setTopicId(random.nextInt(2) + 1);
            makeRoomRequest.setMaxScore(300);
            Room room = roomService.makeRoom("", makeRoomRequest);
            rooms.put(room, new ArrayList<>());
        });

        for (var entry : rooms.entrySet()) {
            System.out.println("Room: " + entry.getKey() + " has " + entry.getValue().size() + " clients.");
        }
    }

    public Room getRoomDetailById(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getKey();
            }
        }
        return null; // Return null or throw an exception if no room with the given ID is found
    }

    public List<Player> getRoomById(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue();
            }
        }
        return null; // Return null or throw an exception if no room with the given ID is found
    }

    public void addUserToRoomById(int roomId, Player user) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().add(user);
            }
        }
    }

    public boolean isPlayerInRoom(int roomId, int userId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if (entry.getKey().getRoomId() == roomId) {
                return entry.getValue().stream().anyMatch(user -> user.getId() == userId);
            }
        }
        return false;
    }

    public void removeUserFromRoom(int roomId, Player user) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().remove(user);
            }
        }
    }

    public void removeUserFromRoomWithId(int roomId, int userId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().removeIf(user -> user.getId() == userId);
            }
        }
    }

    public void removeLastUserFromRoom(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().removeLast();
            }
        }
    }

    public boolean roomExists(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoomFull(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue().size() >= ROOM_CAPACITY;
            }
        }
        return false;
    }

    public boolean addRoom(Room room) {
        if (!rooms.containsKey(room)) {
            rooms.put(room, new ArrayList<>());
            System.out.println("Added room: " + room);
            return true;
        }
        return false;
    }

    public boolean removeRoom(Room room) {
        if (rooms.containsKey(room)) {
            rooms.remove(room);
            System.out.println("Removed room: " + room);
            return true;
        }
        return false;
    }

    public boolean removeRoomWithId(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                rooms.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public Player getRandomUserFromRoom(int roomId) {
        for (Map.Entry<Room, List<Player>> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue().get(random.nextInt(entry.getValue().size()));
            }
        }
        return null;
    }
}
