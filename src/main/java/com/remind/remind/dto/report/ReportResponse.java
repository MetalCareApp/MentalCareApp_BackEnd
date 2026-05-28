package com.remind.remind.dto.report;

import com.remind.remind.domain.report.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "AI 리포트 응답 데이터")
public class ReportResponse {
    @Schema(description = "리포트 ID", example = "1")
    private Long id;

    @Schema(description = "매칭 ID", example = "1")
    private Long matchId;

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

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .matchId(report.getMatch().getId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .content(report.getContent())
                .totalScore(report.getTotalScore())
                .riskLevel(report.getRiskLevel())
                .phq9Slots(report.getPhq9Slots())
                .treatmentRecommendation(report.getTreatmentRecommendation())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
