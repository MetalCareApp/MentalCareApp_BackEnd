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
    private String content;
    private boolean isRisk;
    private LocalDateTime createdAt;

    public static ChatResponse from(ChatMessage message) {
        return ChatResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .isRisk(message.isRisk())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
