package com.backend.socket.model;

import lombok.Getter;
import lombok.Setter;

import static com.backend.constant.Constant.SVG_AVATAR_NAMES;

@Setter
@Getter
public class Player {
    Integer id;
    Boolean currentTurn;
    Boolean nextTurn;
    Integer points;

    Detail detail;

    public Player(Integer id, Boolean currentTurn, Boolean nextTurn, Integer points, Detail detail) {
        this.id = id;
        this.currentTurn = currentTurn;
        this.nextTurn = nextTurn;
        this.points = points;
        this.detail = detail;
    }

    public Player() {
    }

}
