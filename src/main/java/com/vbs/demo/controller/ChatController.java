package com.vbs.demo.controller;

import com.vbs.demo.models.ChatMessage;
import com.vbs.demo.repositories.ChatMessageRepo;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class ChatController {

    private final ChatMessageRepo chatMessageRepo;

    public ChatController(ChatMessageRepo chatMessageRepo) {
        this.chatMessageRepo = chatMessageRepo;
    }

    // Real-time: receives message, saves to DB, broadcasts to room
    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage sendMessage(
            @DestinationVariable String roomId,
            @Payload ChatMessage message) {
        message.setRoomId(roomId);
        chatMessageRepo.save(message); // persist to DB
        return message;
    }

    // History: REST endpoint to load past messages for a room
    @GetMapping("/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessage> getHistory(@PathVariable String roomId) {
        return chatMessageRepo.findByRoomIdOrderByIdAsc(roomId);
    }
}