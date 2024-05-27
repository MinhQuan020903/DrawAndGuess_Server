package com.backend.rest.demo;

import com.backend.rest.topic.Topic;
import com.backend.rest.topic.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo-controller")
public class DemoController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> get() {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("greet", "Coc coc! Ai goi do");
        responseBody.put("message", "Ok roi do");

        return ResponseEntity.ok(responseBody);
    }

}
