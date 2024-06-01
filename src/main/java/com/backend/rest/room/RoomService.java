package com.backend.rest.room;

import com.backend.rest.room.dto.MakeRoomRequest;
import com.backend.rest.room.entity.Room;
import com.backend.rest.user.User;
import com.backend.rest.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final EntityManager entityManager;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<Room> getPublicRooms() {
        return roomRepository.findByIsPublic(true);
    }

    public Room makeRoom(String roomOwner, MakeRoomRequest makeRoomRequest) {
        Room newRoom = Room.builder()
                .owner(roomOwner)
                .capacity(makeRoomRequest.getCapacity())
                .isPublic(makeRoomRequest.isPublic())
                .topicId(makeRoomRequest.getTopicId())
                .maxScore(makeRoomRequest.getMaxScore())
                .build();
        return roomRepository.save(newRoom);
    }

    public Room findById(int roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }

    @Transactional
    public void joinRoom(int roomId, String username) throws Exception {
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new Exception("Room not found");
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }

        user.get().setRoom(room);
        userRepository.save(user.get());
    }

    @Transactional
    public void leaveRoom(int roomId, String username) throws Exception {
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new Exception("Room not found");
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }

        user.get().setRoom(null);
        userRepository.save(user.get());
    }
}