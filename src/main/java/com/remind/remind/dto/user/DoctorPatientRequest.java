package com.remind.remind.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorPatientRequest {
    @NotBlank(message = "환자의 이메일은 필수입니다.")
    private String patientEmail;
}
