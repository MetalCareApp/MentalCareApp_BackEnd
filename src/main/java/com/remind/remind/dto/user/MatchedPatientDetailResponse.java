package com.remind.remind.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MatchedPatientDetailResponse {
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime matchCreatedAt;
    private List<ReportSummary> reports;

    @Getter
    @Builder
    public static class ReportSummary {
        private Long id;
        private LocalDateTime createdAt;
    }
}
