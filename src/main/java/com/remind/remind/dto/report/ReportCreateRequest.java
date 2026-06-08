package com.remind.remind.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "AI 리포트 생성 요청 데이터")
public class ReportCreateRequest {
    @NotNull(message = "대상 환자 ID는 필수입니다.")
    @Schema(description = "대상 환자 ID", example = "2")
    private Long patientId;

    @NotNull(message = "시작 날짜는 필수입니다.")
    @Schema(description = "분석 시작 날짜", example = "2024-05-01")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    @Schema(description = "분석 종료 날짜", example = "2024-05-07")
    private LocalDate endDate;
}
