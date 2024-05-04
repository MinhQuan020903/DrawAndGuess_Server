package com.backend.rest.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private String username;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("friend_list")
    private String[] friendList;

    @JsonProperty("friend_requests")
    private String[] friendRequests;

    @JsonProperty("friend_requests_receive")
    private String[] friendRequestsReceive;

}
