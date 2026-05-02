package com.remind.remind.service.report;

import com.remind.remind.domain.report.Report;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.report.ReportRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${chatbot.api.report-url}")
    private String reportApiUrl;

    public ReportResponse createAiReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // AI 서버로 보낼 요청 데이터 구성 (명세에 따라 session_id만 전송)
        Map<String, String> aiRequest = Map.of(
                "session_id", String.valueOf(userId)
        );

        try {
            // AI 서버 호출 (리포트 생성)
            Map<String, Object> aiResponse = restTemplate.postForObject(reportApiUrl, aiRequest, Map.class);
            
            String content = "리포트를 생성할 수 없습니다.";
            if (aiResponse != null && aiResponse.containsKey("answer")) {
                content = (String) aiResponse.get("answer");
            } else if (aiResponse != null && aiResponse.containsKey("message")) {
                content = (String) aiResponse.get("message");
            }

            Report report = Report.builder()
                    .user(user)
                    .content(content)
                    .build();

            return ReportResponse.from(reportRepository.save(report));
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
