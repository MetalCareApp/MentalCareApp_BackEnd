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

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .matchId(report.getMatch().getId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
