package com.remind.remind.service.chat;

import com.remind.remind.dto.chat.ChatResponse;
import com.remind.remind.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatResponse> getChatHistory(Long userId, int limit) {
        List<ChatResponse> history = chatMessageRepository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(ChatResponse::from)
                .collect(Collectors.toList());
        
        // 최신순으로 가져온 뒤 대화 흐름을 위해 시간순으로 반전
        Collections.reverse(history);
        return history;
    }
}
