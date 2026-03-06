package com.takonline.takonline.websocket.controller;

import com.takonline.takonline.room.dto.RoomResponse;
import com.takonline.takonline.room.service.RoomService;
import com.takonline.takonline.websocket.dto.RoomConnectionRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class RoomWebSocketController {

    private final RoomService roomService;

    public RoomWebSocketController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/rooms/{code}/connect")
    @SendTo("/topic/rooms/{code}")
    public RoomResponse connectPlayer(
            @DestinationVariable String code,
            @Payload RoomConnectionRequest request
    ) {
        return roomService.connectPlayer(code, request.getPlayerName());
    }
}
