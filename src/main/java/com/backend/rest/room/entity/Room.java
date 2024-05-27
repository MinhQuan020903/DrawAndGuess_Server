package com.backend.rest.room.entity;

import com.backend.rest.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_room")
public class Room {
    @Id
    @GeneratedValue
    @Column(name = "room_id")
    private int roomId;

    private String owner;

    private int capacity;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "topic_id")
    private int topicId;

    @Column(name = "max_score")
    private int maxScore;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<User> usersInRoom;
}
