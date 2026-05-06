package com.remind.remind.service.report;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.report.Report;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.report.ReportCreateRequest;
import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.diary.DiaryRepository;
import com.remind.remind.repository.report.ReportRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final RestTemplate restTemplate;

    @Value("${chatbot.api.report-url}")
    private String reportApiUrl;

    public ReportResponse createAiReport(Long userId, ReportCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 요청으로 받은 시작일과 종료일을 사용
        List<Diary> diaries = diaryRepository.findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(
                user, request.getStartDate(), request.getEndDate());

        // AI 서버로 보낼 diary_logs 구성
        List<Map<String, Object>> diaryLogs = diaries.stream().map(diary -> {
            Long sleepMinutes = diary.getTotalSleepMinutes() != null ? diary.getTotalSleepMinutes() : 0L;
            double sleepHours = sleepMinutes / 60.0;
            
            Map<String, Object> log = new java.util.HashMap<>();
            log.put("date", diary.getDiaryDate() != null ? diary.getDiaryDate().toString() : "");
            log.put("emotion", diary.getEmotion() != null ? diary.getEmotion().name() : "");
            log.put("sleepStart", diary.getSleepStartTime() != null ? diary.getSleepStartTime().toString() : "");
            log.put("sleepEnd", diary.getSleepEndTime() != null ? diary.getSleepEndTime().toString() : "");
            log.put("sleepHours", sleepHours);
            log.put("tookMedicine", diary.isMedicationTaken());
            log.put("medicineReaction", diary.getMedicationReaction() != null ? diary.getMedicationReaction() : "");
            log.put("text", diary.getContent() != null ? diary.getContent() : "");
            return log;
        }).collect(Collectors.toList());

        // AI 서버로 보낼 전체 요청 데이터 구성
        Map<String, Object> aiRequest = new java.util.HashMap<>();
        aiRequest.put("session_id", userId);
        aiRequest.put("start_date", request.getStartDate().toString());
        aiRequest.put("end_date", request.getEndDate().toString());
        aiRequest.put("diary_logs", diaryLogs);

        try {
            // AI 서버 호출 (POST /ai/report)
            Map<String, Object> aiResponse = restTemplate.postForObject(reportApiUrl, aiRequest, Map.class);
            
            String content = "리포트를 생성할 수 없습니다.";
            if (aiResponse != null) {
                // specification: AI서버 -> 백 { "summary": "..." }
                if (aiResponse.get("summary") != null) {
                    content = String.valueOf(aiResponse.get("summary"));
                } else if (aiResponse.get("message") != null) {
                    content = String.valueOf(aiResponse.get("message"));
                } else if (aiResponse.get("answer") != null) {
                    content = String.valueOf(aiResponse.get("answer"));
                }
            }

            Report report = Report.builder()
                    .user(user)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .content(content)
                    .build();

            return ReportResponse.from(reportRepository.save(report));
        } catch (Exception e) {
            // 에러 시 로깅 (실제 서비스 시 Logger 사용 권장)
            System.err.println("AI Report Generation Error: " + e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

