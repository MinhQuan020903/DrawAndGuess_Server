package com.backend.rest.friend;

import com.backend.configuration.JwtService;
import com.backend.rest.ResponseGenerator;
import com.backend.rest.friend.dto.FriendResponse;
import com.backend.rest.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final JwtService jwtService;

    @GetMapping("/friendList")
    public ResponseEntity<Map<String, Object>> getFriendList(HttpServletRequest request) {

        final String extractUsername = getUsernameFromRequestToken(request);

        List<User> friends = friendService.getFriendList(extractUsername);
        List<FriendResponse> friendsResponses = friends.stream().map(f -> FriendResponse
                .builder()
                .username(f.getUsername())
                .displayName(f.getDisplayName())
                .isOnline(f.isOnline())
                .build()
        ).toList();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("friend_list", friendsResponses);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/friendRequests")
    public ResponseEntity<Map<String, Object>> getFriendRequests(HttpServletRequest request) {

        final String extractUsername = getUsernameFromRequestToken(request);
        List<User> friendsRequests = friendService.getFriendRequests(extractUsername);
        List<FriendResponse> friendsResponses = friendsRequests.stream().map(f -> FriendResponse
                .builder()
                .username(f.getUsername())
                .displayName(f.getDisplayName())
                .isOnline(f.isOnline())
                .build()
        ).toList();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("friend_requests", friendsResponses);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/friendRequestsReceive")
    public ResponseEntity<Map<String, Object>> getFriendRequestsReceive(HttpServletRequest request) {

        final String extractUsername = getUsernameFromRequestToken(request);
        List<User> friendsRequests = friendService.getFriendRequestsReceive(extractUsername);
        List<FriendResponse> friendsResponses = friendsRequests.stream().map(f -> FriendResponse
                .builder()
                .username(f.getUsername())
                .displayName(f.getDisplayName())
                .isOnline(f.isOnline())
                .build()
        ).toList();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("friend_requests_receive", friendsResponses);

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/sendRequest/{receiverUsername}")
    public ResponseEntity<String> sendFriendRequest(HttpServletRequest request, @PathVariable String receiverUsername) throws Exception {
        final String senderUsername = getUsernameFromRequestToken(request);
        friendService.addFriendRequest(senderUsername, receiverUsername);
        return ResponseEntity.ok(senderUsername + " sent a request to " + receiverUsername);
    }

    @PostMapping("/acceptRequest/{receiverUsername}")
    public ResponseEntity<String> acceptFriendRequest(HttpServletRequest request, @PathVariable String receiverUsername) throws Exception {
        final String senderUsername = getUsernameFromRequestToken(request);
        friendService.acceptFriendRequest(senderUsername, receiverUsername);
        return ResponseEntity.ok(senderUsername + " and " + receiverUsername + " are friends now.");
    }

    private String getUsernameFromRequestToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        final String jwt = authHeader.substring(7);
        return jwtService.extractUsername(jwt);
    }

    @ExceptionHandler
    private ResponseEntity<Map<String, Object>> handleException(Exception exc) {

        HttpStatus status = switch (exc.getMessage()) {
            case "User not found" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };

        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), status);
    }
}
