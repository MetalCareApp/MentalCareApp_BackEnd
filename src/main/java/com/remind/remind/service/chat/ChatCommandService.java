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

    @Value("${chatbot.api.url}")
    private String chatbotApiUrl;

    // 간단한 위험 키워드 목록 (실제 서비스 시 고도화 필요)
    private static final List<String> RISK_KEYWORDS = Arrays.asList("죽고 싶어", "자해", "살기 싫어", "죽음", "자살");

    public ChatResponse saveUserMessage(Long userId, ChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean hasRisk = detectRisk(request.getMessage());

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(ChatMessageRole.USER)
                .message(request.getMessage())
                .hasRisk(hasRisk)
                .build();

        return ChatResponse.from(chatMessageRepository.save(message));
    }

    public ChatResponse callAiAndSaveResponse(Long userId, String userQuestion) {
        // AI 서버로 보낼 요청 데이터 구성 (session_id는 사용자 ID 사용)
        Map<String, String> aiRequest = Map.of(
                "question", userQuestion,
                "session_id", String.valueOf(userId)
        );

        try {
            // AI 서버 호출
            Map<String, Object> aiResponse = restTemplate.postForObject(chatbotApiUrl, aiRequest, Map.class);
            
            String answer = "죄송합니다. 답변을 생성하지 못했습니다.";
            if (aiResponse != null && aiResponse.containsKey("answer")) {
                answer = (String) aiResponse.get("answer");
            } else if (aiResponse != null && aiResponse.containsKey("message")) {
                answer = (String) aiResponse.get("message");
            }

            return saveAssistantMessage(userId, answer);
        } catch (Exception e) {
            // AI 서버 호출 실패 시 기본 답변 저장
            return saveAssistantMessage(userId, "현재 AI 상담이 원활하지 않습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public ChatResponse saveAssistantMessage(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // AI 답변에서도 위험 요소가 있을 수 있으므로 체크
        boolean hasRisk = detectRisk(content);

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(ChatMessageRole.ASSISTANT)
                .message(content)
                .hasRisk(hasRisk)
                .build();

        return ChatResponse.from(chatMessageRepository.save(message));
    }

    private boolean detectRisk(String content) {
        if (content == null) return false;
        return RISK_KEYWORDS.stream().anyMatch(content::contains);
    }
}
