package com.backend.rest.user;

import com.backend.configuration.JwtService;
import com.backend.rest.ResponseGenerator;
import com.backend.rest.user.dto.UserResponse;
import com.backend.rest.user.dto.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable String username, HttpServletRequest request) throws Exception {
        final String authHeader = request.getHeader("Authorization");

        final String jwt = authHeader.substring(7);
        final String extractUsername = jwtService.extractUsername(jwt);

        User matchingUser = userService.findByUsername(username);
        if (matchingUser != null) {
            UserResponse responseBody;
            if (extractUsername.equals(username)) {
                /* Client muốn truy xuất thông tin của chính mình
                -> Full Info */
                responseBody = UserResponse
                        .builder()
                        .username(username)
                        .displayName(matchingUser.getDisplayName())
                        .isOnline(matchingUser.isOnline())
                        .friendList(matchingUser.getFriendList())
                        .friendRequests(matchingUser.getFriendRequests())
                        .friendRequestsReceive(matchingUser.getFriendRequestsReceive())
                        .build();

            } else {
                /* Client muốn truy xuất thông tin của của một user khác
                -> Partial Info */
                responseBody = UserResponse
                        .builder()
                        .username(username)
                        .displayName(matchingUser.getDisplayName())
                        .isOnline(matchingUser.isOnline())
                        .build();
            }
            return ResponseEntity.ok(responseBody);

        } else {
            throw new Exception("User not found");
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateUserInfo(
            @RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request
    ) throws Exception {
        final String authHeader = request.getHeader("Authorization");

        final String jwt = authHeader.substring(7);
        final String extractUsername = jwtService.extractUsername(jwt);

        if (extractUsername.equals(userUpdateRequest.getUsername())) {

            User matchingUser = userService.findByUsername(userUpdateRequest.getUsername());
            matchingUser.setDisplayName(userUpdateRequest.getDisplayName());
            matchingUser.setFriendList(userUpdateRequest.getFriendList());
            matchingUser.setFriendRequests(userUpdateRequest.getFriendRequests());
            matchingUser.setFriendRequestsReceive(userUpdateRequest.getFriendRequestsReceive());

            User updatedUser = userService.save(matchingUser);

            UserResponse responseBody = UserResponse
                    .builder()
                    .username(updatedUser.getUsername())
                    .displayName(updatedUser.getDisplayName())
                    .isOnline(updatedUser.isOnline())
                    .friendList(updatedUser.getFriendList())
                    .friendRequests(updatedUser.getFriendRequests())
                    .friendRequestsReceive(updatedUser.getFriendRequestsReceive())
                    .build();

            return ResponseEntity.ok(responseBody);

        } else {
            throw new Exception("You cannot edit other users' information");
        }

    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleException(Exception exc) {

        HttpStatus status = switch (exc.getMessage()) {
            case "User not found" -> HttpStatus.NOT_FOUND;
            case "You cannot edit other users' information" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), status);
    }
}
