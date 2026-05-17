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

import com.remind.remind.domain.user.Match;
import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.repository.user.MatchRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final MatchRepository matchRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * AI 서버 리포트 응답을 담기 위한 DTO
     */
    @lombok.Setter
    @lombok.Getter
    @lombok.NoArgsConstructor
    private static class AiReportResponse {
        private String summary;
        private String message;
        private String answer;
    }

    public ReportResponse createAiReport(Long userId, ReportCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 해당 유저의 수락된 매칭 확인
        Match match = matchRepository.findAllByPatientIdAndStatus(userId, MatchStatus.ACCEPTED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR)); // TODO: MATCH_NOT_FOUND 추가 필요

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
            // AI 서버 호출 (POST {aiServerUrl}/ai/report)
            String reportApiUrl = aiServerUrl + "/ai/report";
            AiReportResponse aiResponse = restTemplate.postForObject(reportApiUrl, aiRequest, AiReportResponse.class);
            
            String content = "리포트를 생성할 수 없습니다.";
            if (aiResponse != null) {
                // specification: AI서버 -> 백 { "summary": "..." }
                if (aiResponse.getSummary() != null) {
                    content = aiResponse.getSummary();
                } else if (aiResponse.getMessage() != null) {
                    content = aiResponse.getMessage();
                } else if (aiResponse.getAnswer() != null) {
                    content = aiResponse.getAnswer();
                }
            }

            Report report = Report.builder()
                    .match(match)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .content(content)
                    .build();

            return ReportResponse.from(reportRepository.save(report));
        } catch (Exception e) {
            // 에러 시 로깅
            System.err.println("AI Report Generation Error: " + e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

