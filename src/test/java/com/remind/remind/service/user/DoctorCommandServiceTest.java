package com.remind.remind.service.user;

import com.remind.remind.domain.user.*;
import com.remind.remind.dto.user.MatchResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.user.DoctorRepository;
import com.remind.remind.repository.user.MatchRepository;
import com.remind.remind.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorCommandServiceTest {

    @InjectMocks
    private DoctorCommandService doctorCommandService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private MatchRepository matchRepository;

    private User doctorUser;
    private Doctor doctor;
    private User patientUser;

    @BeforeEach
    void setUp() {
        doctorUser = User.builder()
                .email("doctor@test.com")
                .name("Doctor Name")
                .role(Role.DOCTOR)
                .build();
        ReflectionTestUtils.setField(doctorUser, "id", 1L);

        com.remind.remind.domain.hospital.Hospital hospital = com.remind.remind.domain.hospital.Hospital.builder()
                .name("Test Hospital")
                .build();

        doctor = Doctor.builder()
                .user(doctorUser)
                .hospital(hospital)
                .build();
        ReflectionTestUtils.setField(doctor, "id", 1L);

        patientUser = User.builder()
                .email("patient@test.com")
                .name("Patient Name")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(patientUser, "id", 2L);
    }

    @Test
    @DisplayName("이미 거절된 매칭이 있는 경우 다시 요청하면 성공해야 함")
    void requestMatching_WhenAlreadyRejected_ShouldSucceed() {
        // given
        String patientEmail = "patient@test.com";
        given(userRepository.findByEmail(patientEmail)).willReturn(Optional.of(patientUser));
        
        Match rejectedMatch = Match.builder()
                .doctor(doctor)
                .patient(patientUser)
                .status(MatchStatus.REJECTED)
                .build();
        
        given(matchRepository.findByDoctorIdAndPatientId(doctor.getId(), patientUser.getId()))
                .willReturn(Optional.of(rejectedMatch));

        // when
        MatchResponse response = doctorCommandService.requestPatientMatch(doctor, patientEmail);

        // then
        assertThat(response.getStatus()).isEqualTo(MatchStatus.PENDING);
        assertThat(rejectedMatch.getStatus()).isEqualTo(MatchStatus.PENDING);
    }

    @Test
    @DisplayName("이미 수락된 매칭이 있는 경우 다시 요청하면 DR002 발생")
    void requestMatching_WhenAlreadyAccepted_ShouldThrowException() {
        // given
        String patientEmail = "patient@test.com";
        given(userRepository.findByEmail(patientEmail)).willReturn(Optional.of(patientUser));
        
        Match acceptedMatch = Match.builder()
                .doctor(doctor)
                .patient(patientUser)
                .status(MatchStatus.ACCEPTED)
                .build();
        
        given(matchRepository.findByDoctorIdAndPatientId(doctor.getId(), patientUser.getId()))
                .willReturn(Optional.of(acceptedMatch));

        // when & then
        assertThatThrownBy(() -> doctorCommandService.requestPatientMatch(doctor, patientEmail))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_MAPPED);
    }

    @Test
    @DisplayName("이미 대기 중인 매칭이 있는 경우 다시 요청하면 DR002 발생")
    void requestMatching_WhenAlreadyPending_ShouldThrowException() {
        // given
        String patientEmail = "patient@test.com";
        given(userRepository.findByEmail(patientEmail)).willReturn(Optional.of(patientUser));
        
        Match pendingMatch = Match.builder()
                .doctor(doctor)
                .patient(patientUser)
                .status(MatchStatus.PENDING)
                .build();
        
        given(matchRepository.findByDoctorIdAndPatientId(doctor.getId(), patientUser.getId()))
                .willReturn(Optional.of(pendingMatch));

        // when & then
        assertThatThrownBy(() -> doctorCommandService.requestPatientMatch(doctor, patientEmail))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_MAPPED);
    }
}
