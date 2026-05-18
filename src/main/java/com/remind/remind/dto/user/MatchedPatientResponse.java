package com.remind.remind.dto.user;

import com.remind.remind.domain.user.Match;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatchedPatientResponse {
    private Long matchId;
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registeredAt;

    public static MatchedPatientResponse from(Match match) {
        return MatchedPatientResponse.builder()
                .matchId(match.getId())
                .userId(match.getPatient().getId())
                .name(match.getPatient().getName())
                .email(match.getPatient().getEmail())
                .registeredAt(match.getCreatedAt())
                .build();
    }
}
