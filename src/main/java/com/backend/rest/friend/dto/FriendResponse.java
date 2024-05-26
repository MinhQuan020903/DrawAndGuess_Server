package com.backend.rest.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendResponse {

    private String username;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("is_online")
    private boolean isOnline;
}
