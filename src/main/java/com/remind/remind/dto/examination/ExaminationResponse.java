package com.remind.remind.dto.examination;

import com.remind.remind.domain.examination.Examination;
import com.remind.remind.domain.examination.ExaminationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExaminationResponse {
    private Long id;
    private ExaminationType type;
    private Integer score;
    private String severity;
    private LocalDateTime createdAt;

    public static ExaminationResponse from(Examination examination) {
        return ExaminationResponse.builder()
                .id(examination.getId())
                .type(examination.getType())
                .score(examination.getScore())
                .severity(examination.getSeverity())
                .createdAt(examination.getCreatedAt())
                .build();
    }
}
