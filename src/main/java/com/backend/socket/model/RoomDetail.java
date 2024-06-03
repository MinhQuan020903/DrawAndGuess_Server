package com.backend.socket.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class RoomDetail {
    private List<Player> players;
    private Player drawer;
    private String keyword;
    private Integer countCorrectGuess;

    public RoomDetail() {
        this.players = new ArrayList<>();
        this.drawer = new Player();
        this.keyword = "";
        this.countCorrectGuess = 0;
    }

    public RoomDetail(List<Player> players, Player drawer, String keyword, Integer countCorrectGuess) {
        this.players = players;
        this.drawer = drawer;
        this.keyword = keyword;
        this.countCorrectGuess = countCorrectGuess;
    }
}
