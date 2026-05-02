package com.remind.remind.dto.user;

import com.remind.remind.domain.user.Mapping;
import com.remind.remind.domain.user.MappingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MappingResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String hospitalName;
    private Long patientId;
    private String patientNickname;
    private MappingStatus status;
    private LocalDateTime createdAt;

    public static MappingResponse from(Mapping mapping) {
        return MappingResponse.builder()
                .id(mapping.getId())
                .doctorId(mapping.getDoctor().getId())
                .doctorName(mapping.getDoctor().getUser().getNickname())
                .hospitalName(mapping.getDoctor().getHospital().getName())
                .patientId(mapping.getPatient().getId())
                .patientNickname(mapping.getPatient().getNickname())
                .status(mapping.getStatus())
                .createdAt(mapping.getCreatedAt())
                .build();
    }
}
