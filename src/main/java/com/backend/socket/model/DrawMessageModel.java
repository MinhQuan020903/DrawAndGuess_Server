package com.backend.socket.model;

public class DrawMessageModel {
    public record Message(
            String message) {

        String getMessage() {
            return message;
        }
    };

    public record GuessMessage(
            Integer userId, String message, String guessKeyword,  Boolean isCorrect, Integer guessPoint) {

        @Override
        public Integer userId() {
            return userId;
        }
        @Override
        public String message() {
            return message;
        }

        @Override
        public String guessKeyword() {
            return guessKeyword;
        }



        @Override
        public Boolean isCorrect() {
            return isCorrect;
        }

        @Override
        public Integer guessPoint() {
            return guessPoint;
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
