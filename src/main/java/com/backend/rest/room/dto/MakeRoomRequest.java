package com.backend.rest.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MakeRoomRequest {

    private int capacity;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("topic_id")
    private int topicId;

    @JsonProperty("max_score")
    private int maxScore;

    /*
    {
      "capacity": 10,
      "is_public": true,
      "topic_id": 1,
      "max_score": 100
    }
     */
}
