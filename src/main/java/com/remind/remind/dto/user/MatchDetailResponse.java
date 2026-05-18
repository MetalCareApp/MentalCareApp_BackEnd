package com.remind.remind.dto.user;

import com.remind.remind.domain.user.Match;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MatchDetailResponse {
    private Long matchId;
    private String hospitalName;
    private String address;
    private String phone;
    private String doctorName;
    private LocalDate openingAt;
    private LocalDateTime createdAt;

    public static MatchDetailResponse from(Match match) {
        return MatchDetailResponse.builder()
                .matchId(match.getId())
                .hospitalName(match.getDoctor().getHospital().getName())
                .address(match.getDoctor().getHospital().getAddress())
                .phone(match.getDoctor().getHospital().getPhone())
                .doctorName(match.getDoctor().getUser().getName())
                .openingAt(match.getDoctor().getHospital().getOpeningDate())
                .createdAt(match.getCreatedAt())
                .build();
    }
}
