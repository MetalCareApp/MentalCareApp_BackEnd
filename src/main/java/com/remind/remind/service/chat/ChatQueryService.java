package com.remind.remind.service.chat;

import com.remind.remind.dto.chat.ChatResponse;
import com.remind.remind.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        // 최근 대화 N개를 가져온 뒤, 화면 표시를 위해 시간순(Asc)으로 뒤집음
        java.util.Collections.reverse(history);
        return history;
    }
}
