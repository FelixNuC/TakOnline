package com.takonline.takonline.websocket.controller;

import com.takonline.takonline.websocket.dto.RoomMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class RoomWebSocketController {

    @MessageMapping("/rooms/{code}/chat")
    @SendTo("/topic/rooms/{code}")
    public RoomMessage sendRoomMessage(@DestinationVariable String code, RoomMessage message) {
        return message;
    }
}
