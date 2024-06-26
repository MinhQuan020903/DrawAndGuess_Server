package com.backend.socket.singleton;

import com.backend.rest.room.RoomService;
import com.backend.rest.room.dto.MakeRoomRequest;
import com.backend.rest.room.entity.Room;
import com.backend.rest.topic.TopicService;
import com.backend.socket.model.Player;
import com.backend.socket.model.RoomDetail;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

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
    private final ConcurrentHashMap<Room, RoomDetail> rooms = new ConcurrentHashMap<>();
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
            makeRoomRequest.setTopicId(random.nextInt(6) + 1);
            makeRoomRequest.setMaxScore(300);
            Room room = roomService.makeRoom("", makeRoomRequest);
            rooms.put(room, new RoomDetail());
        });

        for (var entry : rooms.entrySet()) {
            System.out.println("Room: " + entry.getKey() + " has " + entry.getValue().getPlayers().size() + " clients.");
        }
    }

    public Room getRoomById(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getKey();
            }
        }
        return null; // Return null or throw an exception if no room with the given ID is found
    }

    public RoomDetail getRoomDetailById(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue();
            }
        }
        return null; // Return null or throw an exception if no room with the given ID is found
    }

    public List<Player> getRoomPlayersById(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue().getPlayers();
            }
        }
        return null; // Return null or throw an exception if no room with the given ID is found
    }

    public void addUserToRoom(int roomId, Player user, String socketId) throws Exception {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().getPlayers().add(user);
                roomService.joinRoom(roomId, user.getDetail().getUsername());
            }
        }
    }

    public boolean isPlayerInRoom(int roomId, int userId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if (entry.getKey().getRoomId() == roomId) {
                return entry.getValue().getPlayers().stream().anyMatch(user -> user.getId() == userId);
            }
        }
        return false;
    }

    public void removeUserFromRoom(int roomId, Player user) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().getPlayers().remove(user);
            }
        }
    }

    public void removeUserFromRoom(int roomId, String username) throws Exception {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().getPlayers().removeIf(user -> user.getDetail().getUsername().equalsIgnoreCase(username));
                roomService.leaveRoom(roomId, username);
            }
        }
    }

    public void removeLastUserFromRoom(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                entry.getValue().getPlayers().removeLast();
            }
        }
    }

    public boolean roomExists(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoomFull(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue().getPlayers().size() >= ROOM_CAPACITY;
            }
        }
        return false;
    }

    public Room createRoom(Integer topicId, Integer capacity, Integer maxScore, Boolean isPublic, String username) {
        MakeRoomRequest makeRoomRequest = new MakeRoomRequest();
        makeRoomRequest.setCapacity(capacity);
        makeRoomRequest.setPublic(isPublic);
        makeRoomRequest.setTopicId(topicId);
        makeRoomRequest.setMaxScore(maxScore);
        Room room = roomService.makeRoom(username, makeRoomRequest);
        rooms.put(room, new RoomDetail());
        return room;
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
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                rooms.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public Player getRandomUserFromRoom(int roomId) {
        for (Map.Entry<Room, RoomDetail> entry : rooms.entrySet()) {
            if ((entry.getKey().getRoomId()) == (roomId)) {
                return entry.getValue().getPlayers().get(random.nextInt(entry.getValue().getPlayers().size()));
            }
        }
        return null;
    }
}
