package com.remind.remind.service.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final MatchRepository matchRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.report.url}")
    private String aiServerUrl;

    /**
     * AI 서버 리포트 응답을 담기 위한 DTO
     */
    @lombok.Setter
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiReportResponse {
        private String summary;
        private List<Integer> phq9_slots;
        private Integer total_score;
        private String risk_level;
        private String treatment_recommendation;
    }

    public ReportResponse createAiReport(Long requesterId, ReportCreateRequest request) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!requester.isDoctor()) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // 의사와 환자 사이의 수락된 매칭 확인
        Match match = matchRepository.findByDoctorUserIdAndPatientIdAndStatus(requesterId, request.getPatientId(), MatchStatus.ACCEPTED)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        User patient = match.getPatient();

        // 요청으로 받은 시작일과 종료일을 사용
        List<Diary> diaries = diaryRepository.findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(
                patient, request.getStartDate(), request.getEndDate());
        
        log.info("Doctor {} requested report for patient {}. Found {} diaries between {} and {}", 
                requesterId, request.getPatientId(), diaries.size(), request.getStartDate(), request.getEndDate());

        // AI 서버로 보낼 diary_logs 구성 (snake_case)
        // ... (rest of logic same)
        List<Map<String, Object>> diaryLogs = diaries.stream().map(diary -> {
            Long sleepMinutes = diary.getTotalSleepMinutes() != null ? diary.getTotalSleepMinutes() : 0L;
            double sleepHours = sleepMinutes / 60.0;
            
            Map<String, Object> logMap = new java.util.HashMap<>();
            logMap.put("date", diary.getDiaryDate() != null ? diary.getDiaryDate().toString() : "");
            logMap.put("emotion", diary.getEmotion() != null ? diary.getEmotion().name() : "");
            logMap.put("sleep_start", diary.getSleepStartTime() != null ? diary.getSleepStartTime().toString() : "");
            logMap.put("sleep_end", diary.getSleepEndTime() != null ? diary.getSleepEndTime().toString() : "");
            logMap.put("sleep_hours", sleepHours);
            logMap.put("took_medicine", diary.isMedicationTaken());
            logMap.put("medicine_reaction", diary.getMedicationReaction() != null ? diary.getMedicationReaction() : "");
            logMap.put("diary", diary.getContent() != null ? diary.getContent() : "");
            return logMap;
        }).collect(Collectors.toList());

        // AI 서버로 보낼 전체 요청 데이터 구성 (snake_case)
        Map<String, Object> aiRequest = new java.util.HashMap<>();
        aiRequest.put("session_id", String.valueOf(patient.getId()));
        aiRequest.put("start_date", request.getStartDate().toString());
        aiRequest.put("end_date", request.getEndDate().toString());
        aiRequest.put("diary_logs", diaryLogs);

        try {
            // AI 서버 호출 (POST {aiServerUrl}/ai/report)
            String reportApiUrl = aiServerUrl + "/ai/report";
            log.info("Requesting AI Report. URL: {}, RequestBody: {}", reportApiUrl, aiRequest);
            
            AiReportResponse aiResponse = null;
            String content = "리포트를 생성할 수 없습니다.";
            try {
                // 상세 로그를 위해 먼저 String으로 받음
                org.springframework.http.ResponseEntity<String> responseEntity = 
                        restTemplate.postForEntity(reportApiUrl, aiRequest, String.class);
                
                log.info("AI Report HTTP Status: {}", responseEntity.getStatusCode());
                String rawBody = responseEntity.getBody();
                log.info("AI Report Raw Response Body: {}", rawBody);
                
                if (rawBody != null && !rawBody.trim().isEmpty()) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    aiResponse = mapper.readValue(rawBody, AiReportResponse.class);
                    log.info("Parsed AI Response: {}", aiResponse);
                    
                    if (aiResponse != null && aiResponse.getSummary() != null) {
                        content = aiResponse.getSummary();
                    } else {
                        log.warn("AI Report Summary is missing in parsed object. Raw body: {}", rawBody);
                    }
                } else {
                    log.warn("AI Report Response body is empty.");
                }
            } catch (Exception e) {
                log.error("AI Report Server Error (Colab/AI): {}", e.getMessage(), e);
                content = "리포트 생성 중입니다. 잠시만 기다려주십시오.";
            }

            Report report = Report.builder()
                    .match(match)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .content(content)
                    .totalScore(aiResponse != null && aiResponse.getTotal_score() != null ? aiResponse.getTotal_score() : 0)
                    .riskLevel(aiResponse != null && aiResponse.getRisk_level() != null ? aiResponse.getRisk_level() : "Unknown")
                    .phq9Slots(aiResponse != null && aiResponse.getPhq9_slots() != null ? aiResponse.getPhq9_slots().toString() : "[]")
                    .treatmentRecommendation(aiResponse != null && aiResponse.getTreatment_recommendation() != null ? aiResponse.getTreatment_recommendation() : "")
                    .build();

            return ReportResponse.from(reportRepository.save(report), diaries);
        } catch (Exception e) {
            log.error("Report generation failed: {}", e.getMessage(), e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 리포트 삭제 (Soft Delete)
     */
    public void deleteReport(Long reportId, Long userId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));

        // 권한 확인: 리포트의 주인이 요청자인지 확인
        if (!report.getMatch().getPatient().getId().equals(userId)) {
            throw new BaseException(ErrorCode.REPORT_ACCESS_DENIED);
        }

        reportRepository.delete(report);
    }
}
