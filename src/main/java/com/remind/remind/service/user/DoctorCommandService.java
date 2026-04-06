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
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (user.isDoctor()) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 1. 병원 데이터 정제 및 조회/생성
        Hospital hospital = findOrCreateHospital(request.getHospitalName(), request.getHospitalAddress(), request.getHospitalPhoneNumber());

        // 2. 역할 승격 및 Doctor 프로필 생성
        user.promoteToDoctor();
        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .specialization(request.getSpecialization())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // 3. 환자 연결 시도 (입력된 경우에만 진행)
        if (StringUtils.hasText(request.getPatientUsername())) {
            requestPatientMapping(savedDoctor, request.getPatientUsername().trim());
        }

        // 4. 역할 승격 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 환자 매핑 요청 로직 (재사용을 위해 분리)
     */
    public void requestPatientMapping(Doctor doctor, String patientEmail) {
        if (doctor.getUser().getUsername().equalsIgnoreCase(patientEmail)) {
            throw new BaseException(ErrorCode.INVALID_PATIENT);
        }

        User patient = userRepository.findByUsername(patientEmail)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (doctorPatientRepository.existsByDoctorIdAndPatientId(doctor.getId(), patient.getId())) {
            throw new BaseException(ErrorCode.ALREADY_MAPPED);
        }

        DoctorPatient mapping = DoctorPatient.builder()
                .doctor(doctor)
                .patient(patient)
                .status(MappingStatus.PENDING)
                .build();

        doctorPatientRepository.save(mapping);
    }

    private Hospital findOrCreateHospital(String name, String address, String phone) {
        String trimmedName = name.trim();
        String trimmedAddress = address.trim();

        return hospitalRepository.findByNameAndAddress(trimmedName, trimmedAddress)
                .orElseGet(() -> hospitalRepository.save(
                        Hospital.builder()
                                .name(trimmedName)
                                .address(trimmedAddress)
                                .phoneNumber(phone)
                                .build()
                ));
    }
}
