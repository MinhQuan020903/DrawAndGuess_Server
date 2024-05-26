package com.backend.rest.room;

import com.backend.rest.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByIsPublic(boolean isPublic);
}
