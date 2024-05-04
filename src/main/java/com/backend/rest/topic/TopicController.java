package com.backend.rest.topic;

import com.backend.rest.topic.dto.TopicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopic() {
        List<Topic> topics = topicService.getAllTopic();
        List<TopicResponse> topicResponses = topics.stream().map(t -> TopicResponse
                .builder()
                .name(t.getName())
                .illustrationUrl(t.getIllustrationUrl())
                .note(t.getNote())
                .build()
        ).toList();

        return ResponseEntity.ok(topicResponses);
    }
}
