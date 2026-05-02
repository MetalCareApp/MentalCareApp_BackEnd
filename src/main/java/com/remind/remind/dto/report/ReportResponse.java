package com.remind.remind.dto.report;

import com.remind.remind.domain.report.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
