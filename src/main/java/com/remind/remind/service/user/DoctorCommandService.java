package com.remind.remind.service.user;

import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.domain.user.*;
import com.remind.remind.dto.user.DoctorSignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.hospital.HospitalRepository;
import com.remind.remind.repository.user.DoctorPatientRepository;
import com.remind.remind.repository.user.DoctorRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorCommandService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorPatientRepository doctorPatientRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 의사로 전환(Upgrade) 및 선택적으로 첫 환자 연결
     */
    public TokenResponse signup(Long currentUserId, DoctorSignupRequest request) {
        // 1. 현재 사용자 조회
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 2. 이미 의사인 경우 중복 방지
        if (user.getRole() == Role.DOCTOR) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 3. 병원 정보 처리
        Hospital hospital = hospitalRepository.findByNameAndAddress(request.getHospitalName(), request.getHospitalAddress())
                .orElseGet(() -> hospitalRepository.save(
                        Hospital.builder()
                                .name(request.getHospitalName())
                                .address(request.getHospitalAddress())
                                .phoneNumber(request.getHospitalPhoneNumber())
                                .build()
                ));

        // 4. 역할 업그레이드 및 Doctor 프로필 생성
        user.promoteToDoctor();
        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .specialization(request.getSpecialization())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // 5. 환자 연결 시도 (입력된 경우에만 진행)
        if (StringUtils.hasText(request.getPatientUsername())) {
            User patient = userRepository.findByUsername(request.getPatientUsername())
                    .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

            DoctorPatient mapping = DoctorPatient.builder()
                    .doctor(savedDoctor)
                    .patient(patient)
                    .status(MappingStatus.PENDING)
                    .build();

            doctorPatientRepository.save(mapping);
        }

        // 6. 역할이 바뀌었으므로 새로운 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
