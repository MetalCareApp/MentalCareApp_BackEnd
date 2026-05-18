package com.remind.remind.dto.hospital;

import com.remind.remind.domain.hospital.Hospital;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {
    private Long id;
    private String name;
    private String apiId;
    private String address;
    private String phone;
    private LocalDate openingDate;
    private Integer specialistCount;
    private Integer generalDoctorCount;
    private boolean isLiked;

    public static HospitalResponse from(Hospital hospital, boolean isLiked) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .apiId(hospital.getApiId())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .openingDate(hospital.getOpeningDate())
                .specialistCount(hospital.getSpecialistCount())
                .generalDoctorCount(hospital.getGeneralDoctorCount())
                .isLiked(isLiked)
                .build();
    }
}
