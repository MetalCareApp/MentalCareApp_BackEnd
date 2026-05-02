package com.remind.remind.repository.chat;

import com.remind.remind.domain.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
