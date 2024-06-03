package com.backend.socket.model;

import lombok.Getter;

@Getter
public class PlayerSocketInfo {
    private Integer roomId;
    private Integer userId;
    public PlayerSocketInfo(Integer roomId, Integer userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

}