package com.remind.remind.dto.user;

import com.remind.remind.domain.user.Match;
import com.remind.remind.domain.user.MatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatchResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String hospitalName;
    private Long patientId;
    private String patientName;
    private MatchStatus status;
    private LocalDateTime createdAt;

    public static MatchResponse from(Match match) {
        return MatchResponse.builder()
                .id(match.getId())
                .doctorId(match.getDoctor().getId())
                .doctorName(match.getDoctor().getUser().getName())
                .hospitalName(match.getDoctor().getHospital().getName())
                .patientId(match.getPatient().getId())
                .patientName(match.getPatient().getName())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .build();
    }
}
