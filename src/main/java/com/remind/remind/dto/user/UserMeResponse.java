package com.remind.remind.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMeResponse {
    private Long userId;
    private String name;
    private String role;
    private String email;
    private Long matchId;
    private DoctorInfoResponse doctorInfo;

    @Getter
    @Builder
    public static class DoctorInfoResponse {
        private HospitalInfoResponse hospital;
        private Integer patientCount;
    }

    @Getter
    @Builder
    public static class HospitalInfoResponse {
        private String name;
        private String address;
        private String phone;
    }
}
