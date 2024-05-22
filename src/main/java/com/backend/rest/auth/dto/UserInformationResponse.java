package com.backend.rest.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.backend.rest.user.Role;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInformationResponse {
    @JsonProperty("id")
    private int id;
    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private Role role;

    @JsonProperty("display_name")
    private String displayName;
}
