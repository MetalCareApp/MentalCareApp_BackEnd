package com.remind.remind.dto.hospital;

import com.remind.remind.domain.hospital.Hospital;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private boolean isLiked;

    public static HospitalResponse from(Hospital hospital, boolean isLiked) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .isLiked(isLiked)
                .build();
    }
}
