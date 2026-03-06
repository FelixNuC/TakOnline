package com.takonline.takonline.room.repository;

import org.springframework.stereotype.Repository;
import com.takonline.takonline.room.model.Room;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomRepository {

    private final Map<String, Room> roomsByCode = new ConcurrentHashMap<>();

    public Room save(Room room) {
        roomsByCode.put(room.getCode(), room);
        return room;
    }

    public Optional<Room> findByCode(String code) {
        return Optional.ofNullable(roomsByCode.get(code));
    }
}