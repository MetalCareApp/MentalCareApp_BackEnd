package com.remind.remind.dto.chat;

import com.remind.remind.domain.chat.ChatMessage;
import com.remind.remind.domain.chat.ChatMessageRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatResponse {
    private Long id;
    private ChatMessageRole role;
    private String message;
    private boolean hasRisk;
    private LocalDateTime createdAt;

    public static ChatResponse from(ChatMessage message) {
        return ChatResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .message(message.getMessage())
                .hasRisk(message.isHasRisk())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
