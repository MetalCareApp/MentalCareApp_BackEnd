package com.remind.remind.dto.user;

import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientSearchResponse {
    private Long userId;
    private String name;
    private String email;
    private MatchStatus matchStatus; // PENDING, ACCEPTED (필터링에 의해 사실상 안나옴), null (기존 요청 없음)

    public static PatientSearchResponse of(User user, MatchStatus status) {
        return PatientSearchResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .matchStatus(status)
                .build();
    }
}
