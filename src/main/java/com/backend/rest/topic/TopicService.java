package com.backend.rest.topic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public Topic save(Topic theTopic) {
        return topicRepository.save(theTopic);
    }

    public List<Topic> getAllTopic() {
        return topicRepository.findAll();
    }

    public Topic getTopicById(int topicId) {
        return topicRepository.findById(topicId).orElse(null);
    }
}
