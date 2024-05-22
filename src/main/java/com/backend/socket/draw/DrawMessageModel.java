package com.backend.socket.draw;

public class DrawMessageModel {
    record Message(
            String message) {

        String getMessage() {
            return message;
        }
    };
    record User(
            String userId,
            boolean isPlayer,
            Object data) {
        @Override
        public String userId() {
            return userId;
        }

        @Override
        public boolean isPlayer() {
            return isPlayer;
        }

        @Override
        public Object data() {
            return data;
        }
        };
    record Room(
            String roomId,
            Integer capacity,
            Integer currentCapacity) {

        @Override
        public String roomId() {
            return roomId;
        }

        @Override
        public Integer capacity() {
            return capacity;
        }

        @Override
        public Integer currentCapacity() {
            return currentCapacity;
        }
    }

}
