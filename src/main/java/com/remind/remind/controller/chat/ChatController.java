package com.remind.remind.controller.chat;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.chat.ChatRequest;
import com.remind.remind.dto.chat.ChatResponse;
import com.remind.remind.service.chat.ChatCommandService;
import com.remind.remind.service.chat.ChatQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/chat")
public class ChatController {

    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;

    /**
     * 사용자의 메시지를 저장하고 AI의 답변을 받아와 저장 후 반환
     */
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ChatRequest request) {
        
        // 1. 사용자 메시지 저장
        chatCommandService.saveUserMessage(principalDetails.getUser().getId(), request);
        
        // 2. AI 서버 호출 및 답변 저장 (session_id는 내부적으로 처리)
        ChatResponse aiResponse = chatCommandService.callAiAndSaveResponse(principalDetails.getUser().getId(), request.getQuestion());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(aiResponse);
    }

    /**
     * 이전 대화 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<ChatResponse> history = chatQueryService.getChatHistory(principalDetails.getUser().getId(), limit);
        return ResponseEntity.ok(history);
    }
}
