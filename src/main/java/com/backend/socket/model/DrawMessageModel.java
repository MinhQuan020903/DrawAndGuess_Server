package com.backend.socket.model;

public class DrawMessageModel {
    public record Message(
            String message) {

        String getMessage() {
            return message;
        }
    };
    public record User(
            int userId,
            boolean isPlayer,
            Object data) {
        @Override
        public int userId() {
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
    public record Room(
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
