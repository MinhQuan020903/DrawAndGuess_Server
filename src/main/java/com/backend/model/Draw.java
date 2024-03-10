package com.backend.model;

import com.backend.enums.DrawType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Draw {

    private String canvasState;

    private Line line;

    private LocalDateTime createdAt;

}
