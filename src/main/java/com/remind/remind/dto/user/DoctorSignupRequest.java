package com.remind.remind.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorSignupRequest {

    @NotBlank(message = "의사 이름은 필수입니다.")
    private String doctorName;

    @NotBlank(message = "병원 이름은 필수입니다.")
    private String hospitalName;

    @NotBlank(message = "병원 번호는 필수입니다.")
    private String hospitalPhone;

    public DoctorSignupRequest(String doctorName, String hospitalName, String hospitalPhone) {
        this.doctorName = doctorName;
        this.hospitalName = hospitalName;
        this.hospitalPhone = hospitalPhone;
    }
}
