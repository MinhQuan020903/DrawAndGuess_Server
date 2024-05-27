package com.backend.rest.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String username;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("is_online")
    private boolean isOnline;

    @JsonProperty("friend_list")
    private List<String> friendList;

    @JsonProperty("friend_requests")
    private List<String> friendRequests;

    @JsonProperty("friend_requests_receive")
    private List<String> friendRequestsReceive;
}
