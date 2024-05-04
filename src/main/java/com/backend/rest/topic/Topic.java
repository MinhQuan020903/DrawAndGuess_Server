package com.backend.rest.topic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_topic")
public class Topic {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    private String illustrationUrl;

    private String note;

    private String[] words;
}
