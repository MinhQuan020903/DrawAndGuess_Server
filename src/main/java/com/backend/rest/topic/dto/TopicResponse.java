package com.backend.rest.topic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponse {

    private String name;

    @JsonProperty("illustration_url")
    private String illustrationUrl;

    private String note;
}
