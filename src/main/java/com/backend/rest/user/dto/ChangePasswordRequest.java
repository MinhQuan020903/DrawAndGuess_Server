package com.backend.rest.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @JsonProperty("current_password")
    private String currentPassword;

    @JsonProperty("new_password")
    private String newPassword;

    @JsonProperty("confirm_password")
    private String confirmPassword;

    /*
    {
        "current_password": "password",
        "new_password": "hoanglam1704",
        "confirm_password": "hoanglam1704"
    }
     */
}
