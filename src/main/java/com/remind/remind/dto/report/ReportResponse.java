package com.remind.remind.dto.report;

import com.remind.remind.domain.report.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "AI 리포트 응답 데이터")
public class ReportResponse {
    @Schema(description = "리포트 ID", example = "1")
    private Long id;

    @Schema(description = "매칭 ID", example = "1")
    private Long matchId;

    @Schema(description = "환자 이름", example = "홍길동")
    private String patientName;

    @Schema(description = "분석 시작 날짜", example = "2024-05-01")
    private LocalDate startDate;

    @Schema(description = "분석 종료 날짜", example = "2024-05-07")
    private LocalDate endDate;

    @Schema(description = "AI 분석 내용 (마크다운 등)", example = "최근 수면 패턴이 불규칙하며...")
    private String content;

    @Schema(description = "PHQ-9 합계 점수", example = "15")
    private Integer totalScore;

    @Schema(description = "위험 수준", example = "High Risk")
    private String riskLevel;

    @Schema(description = "PHQ-9 각 항목별 점수", example = "[1, 3, 2, 0, 1, 2, 3, 0, 1]")
    private String phq9Slots;

    @Schema(description = "치료 권장 사항", example = "충분한 휴식과 상담 치료가 권장됩니다.")
    private String treatmentRecommendation;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "일자별 상세 데이터")
    private List<DailyDiaryDetail> dailyDetails;

    @Getter
    @Builder
    @Schema(description = "일자별 일기 상세 정보")
    public static class DailyDiaryDetail {
        @Schema(description = "날짜")
        private LocalDate date;
        @Schema(description = "감정 점수 (1-5)")
        private int emotionScore;
        @Schema(description = "수면 시간 (시간 단위)")
        private double sleepHours;
        @Schema(description = "복약 여부")
        private boolean medicationTaken;
        @Schema(description = "외부 스트레스 요인 여부")
        private boolean externalStress;
    }

    public static ReportResponse from(Report report, List<com.remind.remind.domain.diary.Diary> diaries) {
        List<DailyDiaryDetail> dailyDetails = diaries.stream()
                .map(diary -> DailyDiaryDetail.builder()
                        .date(diary.getDiaryDate())
                        .emotionScore(diary.getEmotion().getScore())
                        .sleepHours(diary.getTotalSleepMinutes() / 60.0)
                        .medicationTaken(diary.isMedicationTaken())
                        .externalStress(diary.isExternalStress())
                        .build())
                .toList();

        return ReportResponse.builder()
                .id(report.getId())
                .matchId(report.getMatch().getId())
                .patientName(report.getMatch().getPatient().getName())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .content(report.getContent())
                .totalScore(report.getTotalScore())
                .riskLevel(report.getRiskLevel())
                .phq9Slots(report.getPhq9Slots())
                .treatmentRecommendation(report.getTreatmentRecommendation())
                .createdAt(report.getCreatedAt())
                .dailyDetails(dailyDetails)
                .build();
    }
}
