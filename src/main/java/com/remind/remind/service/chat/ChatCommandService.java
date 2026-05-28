package com.remind.remind.service.chat;

import com.remind.remind.domain.chat.ChatMessage;
import com.remind.remind.domain.chat.ChatMessageRole;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.chat.ChatRequest;
import com.remind.remind.dto.chat.ChatResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.chat.ChatMessageRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.chat.url}")
    private String aiServerUrl;

    // 간단한 위험 키워드 목록 (실제 서비스 시 고도화 필요)
    private static final List<String> RISK_KEYWORDS = Arrays.asList("죽고 싶어", "자해", "살기 싫어", "죽음", "자살");

    /**
     * AI 서버 응답을 담기 위한 DTO
     */
    @lombok.Setter
    @lombok.Getter
    @lombok.NoArgsConstructor
    private static class AiChatResponse {
        private String answer;
        private Boolean is_risk;
    }

    public ChatResponse saveUserMessage(Long userId, ChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean isRisk = detectRisk(request.getQuestion());

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(ChatMessageRole.USER)
                .content(request.getQuestion())
                .isRisk(isRisk)
                .build();

        return ChatResponse.from(chatMessageRepository.save(message));
    }

    public ChatResponse callAiAndSaveResponse(Long userId, String userQuestion) {
        // AI 서버로 보낼 요청 데이터 구성 (session_id는 사용자 ID 사용 - String 형식으로 변환)
        Map<String, Object> aiRequest = new java.util.HashMap<>();
        aiRequest.put("question", userQuestion);
        aiRequest.put("session_id", String.valueOf(userId));

        try {
            // AI 서버 호출 (POST {aiServerUrl}/ai/chat)
            String chatApiUrl = aiServerUrl + "/ai/chat";
            AiChatResponse aiResponse = restTemplate.postForObject(chatApiUrl, aiRequest, AiChatResponse.class);
            
            String answer = "죄송합니다. 답변을 생성하지 못했습니다.";
            boolean isRisk = false;

            if (aiResponse != null) {
                // specification: AI서버 -> 백 { "answer": "...", "is_risk": boolean }
                if (aiResponse.getAnswer() != null) {
                    answer = aiResponse.getAnswer();
                }
                
                if (aiResponse.getIs_risk() != null) {
                    isRisk = aiResponse.getIs_risk();
                }
            }

            return saveAssistantMessage(userId, answer, isRisk);
        } catch (Exception e) {
            System.err.println("AI Chatbot Error: " + e.getMessage());
            // AI 서버 호출 실패 시 기본 답변 저장
            return saveAssistantMessage(userId, "현재 AI 상담이 원활하지 않습니다. 잠시 후 다시 시도해주세요.", false);
        }
    }

    public ChatResponse saveAssistantMessage(Long userId, String content, boolean isRisk) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(ChatMessageRole.ASSISTANT)
                .content(content)
                .isRisk(isRisk)
                .build();

        return ChatResponse.from(chatMessageRepository.save(message));
    }

    private boolean detectRisk(String content) {
        if (content == null) return false;
        return RISK_KEYWORDS.stream().anyMatch(content::contains);
    }
}
