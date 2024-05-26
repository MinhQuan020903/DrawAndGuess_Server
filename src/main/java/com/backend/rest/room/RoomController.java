package com.backend.rest.room;

import com.backend.configuration.JwtService;
import com.backend.rest.ResponseGenerator;
import com.backend.rest.room.dto.MakeRoomRequest;
import com.backend.rest.room.entity.Room;
import com.backend.rest.topic.Topic;
import com.backend.rest.topic.TopicService;
import com.backend.rest.topic.dto.TopicResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final TopicService topicService;
    private final JwtService jwtService;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getPublicRooms() {
        List<Room> publicRooms = roomService.getPublicRooms();
        List<Map<String, Object>> responseBody = publicRooms.stream().map(room -> {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("room_id", room.getRoomId());
            roomInfo.put("owner", room.getOwner());
            roomInfo.put("capacity", room.getCapacity());
            roomInfo.put("current_quantity", room.getUsersInRoom().size());

            Topic topic = topicService.getTopicById(room.getTopicId());
            roomInfo.put("topic", TopicResponse.builder()
                    .name(topic.getName())
                    .note(topic.getNote())
                    .illustrationUrl(topic.getIllustrationUrl())
                    .build()
            );
            roomInfo.put("max_score", room.getMaxScore());

            return roomInfo;
        }).toList();
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/makeRoom")
    public ResponseEntity<Map<String, Object>> makeRoom(@RequestBody MakeRoomRequest makeRoomRequest, HttpServletRequest request) {
        final String roomOwner = getUsernameFromRequestToken(request);
        Room newRoom = roomService.makeRoom(roomOwner, makeRoomRequest);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("room_id", newRoom.getRoomId());
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomInfo(@PathVariable int roomId) throws Exception {
        Room matchingRoom = roomService.findById(roomId);
        if (matchingRoom != null) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("room_id", matchingRoom.getRoomId());
            responseBody.put("owner", matchingRoom.getOwner());
            responseBody.put("capacity", matchingRoom.getCapacity());
            responseBody.put("is_public", matchingRoom.isPublic());

            Topic topic = topicService.getTopicById(matchingRoom.getTopicId());
            responseBody.put("topic", TopicResponse.builder()
                    .name(topic.getName())
                    .note(topic.getNote())
                    .illustrationUrl(topic.getIllustrationUrl())
                    .build()
            );
            responseBody.put("max_score", matchingRoom.getMaxScore());

            responseBody.put("users_in_room", matchingRoom.getUsersInRoom().stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", user.getUsername());
                userInfo.put("display_name", user.getDisplayName());
                return userInfo;
            }));
            return ResponseEntity.ok(responseBody);
        } else {
            throw new Exception("Room not found");
        }
    }

    @GetMapping("/{roomId}/members")
    public ResponseEntity<Map<String, Object>> getMembersInRoom(@PathVariable int roomId) throws Exception {
        Room matchingRoom = roomService.findById(roomId);
        if (matchingRoom != null) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("room_id", matchingRoom.getRoomId());
            responseBody.put("users_in_room", matchingRoom.getUsersInRoom().stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", user.getUsername());
                userInfo.put("display_name", user.getDisplayName());
                return userInfo;
            }));
            return ResponseEntity.ok(responseBody);
        } else {
            throw new Exception("Room not found");
        }
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinRoom(@PathVariable int roomId, HttpServletRequest request) throws Exception {
        String extractUsername = getUsernameFromRequestToken(request);
        roomService.joinRoom(roomId, extractUsername);
        return ResponseEntity.ok(extractUsername + " has joined room " + roomId);
    }

    private String getUsernameFromRequestToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        final String jwt = authHeader.substring(7);
        return jwtService.extractUsername(jwt);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleException(Exception exc) {

        HttpStatus status = switch (exc.getMessage()) {
            case "Room not found" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };

        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), status);
    }
}
