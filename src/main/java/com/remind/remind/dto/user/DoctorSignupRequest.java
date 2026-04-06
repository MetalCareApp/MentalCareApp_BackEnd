package com.remind.remind.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorSignupRequest {

    // 병원 정보
    @NotBlank(message = "병원 이름은 필수입니다.")
    private String hospitalName;

    @NotBlank(message = "병원 주소는 필수입니다.")
    private String hospitalAddress;

    private String hospitalPhoneNumber;
    private String specialization;

    // 첫 연결 환자 정보 (선택 사항)
    private String patientUsername;

    public DoctorSignupRequest(String hospitalName, String hospitalAddress, String hospitalPhoneNumber, 
                               String specialization, String patientUsername) {
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.hospitalPhoneNumber = hospitalPhoneNumber;
        this.specialization = specialization;
        this.patientUsername = patientUsername;
    }
}
