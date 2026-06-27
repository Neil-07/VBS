package com.vbs.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;   // "user" or "manager"
    private String content;
    private String roomId;   // customer's user ID as string
    private String timestamp;

    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}