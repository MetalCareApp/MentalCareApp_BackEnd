package com.remind.remind.dto.report;

import com.remind.remind.domain.report.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private Long matchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String content;
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
