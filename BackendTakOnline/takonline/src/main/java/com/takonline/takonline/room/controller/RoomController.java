package com.takonline.takonline.room.controller;

import com.takonline.takonline.room.dto.CreateRoomRequest;
import com.takonline.takonline.room.dto.CreateRoomResponse;
import com.takonline.takonline.room.dto.JoinRoomRequest;
import com.takonline.takonline.room.dto.RoomResponse;
import com.takonline.takonline.room.service.RoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

  @PostMapping
public RoomResponse createRoom(@RequestBody CreateRoomRequest request) {
    return roomService.createRoom(request.getPlayerName(), request.getBoardSize());
}

    @PostMapping("/{code}/join")
    public RoomResponse joinRoom(@PathVariable String code, @RequestBody JoinRoomRequest request) {
        return roomService.joinRoom(code, request.getPlayerName());
    }

    @GetMapping("/{code}")
    public RoomResponse getRoom(@PathVariable String code) {
        return roomService.getRoom(code);
    }
}