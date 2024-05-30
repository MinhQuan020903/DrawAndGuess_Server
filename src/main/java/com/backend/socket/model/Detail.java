package com.backend.socket.model;

import lombok.Getter;
import lombok.Setter;

import static com.backend.constant.Constant.SVG_AVATAR_NAMES;

@Setter
@Getter
public class Detail {
    String username;
    String avatar;

    public Detail(String username) {
        this.username = username;
        this.avatar = "https://api.dicebear.com/5.x/big-smile/svg?seed=$" + SVG_AVATAR_NAMES[(int) (Math.random() * SVG_AVATAR_NAMES.length)];
    }

    public Detail() {
    }

}
