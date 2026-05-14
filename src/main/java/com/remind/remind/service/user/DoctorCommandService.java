package com.remind.remind.service.user;

import com.remind.remind.config.jwt.JwtTokenProvider;
import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.domain.user.*;
import com.remind.remind.dto.user.DoctorSignupRequest;
import com.remind.remind.dto.user.TokenResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.hospital.HospitalRepository;
import com.remind.remind.repository.user.MatchRepository;
import com.remind.remind.repository.user.DoctorRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.remind.remind.dto.user.MatchRequest;
import com.remind.remind.dto.user.MatchResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorCommandService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final MatchRepository matchRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 의사가 환자에게 매칭 요청 전송
     */
    public MatchResponse requestMatching(Long currentUserId, MatchRequest request) {
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return requestPatientMatch(doctor, request.getPatientEmail().trim());
    }

    /**
     * 매칭 상태 변경 (수락/거절)
     */
    public MatchResponse updateMatchStatus(Long matchId, MatchStatus status, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        // 권한 체크: 수락/거절은 해당 환자만 가능
        if (!match.getPatient().getId().equals(currentUserId)) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        match.updateStatus(status);
        return MatchResponse.from(match);
    }

    /**
     * 매칭 삭제 (연결 해제)
     */
    public void deleteMatch(Long matchId, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        // 권한 체크: 당사자(의사 또는 환자)만 삭제 가능
        boolean isDoctorOwner = doctorRepository.findByUserId(currentUserId)
                .map(d -> d.getId().equals(match.getDoctor().getId())).orElse(false);
        boolean isPatientOwner = match.getPatient().getId().equals(currentUserId);

        if (!isDoctorOwner && !isPatientOwner) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        matchRepository.delete(match);
    }

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
        Hospital hospital = findOrCreateHospital(request.getHospitalName(), request.getHospitalAddress(), request.getHospitalPhone());

        // 2. 역할 승격 및 Doctor 프로필 생성
        user.promoteToDoctor();
        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .specialization(request.getSpecialization())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // 3. 환자 연결 시도 (입력된 경우에만 진행)
        if (StringUtils.hasText(request.getPatientEmail())) {
            requestPatientMatch(savedDoctor, request.getPatientEmail().trim());
        }

        // 4. 역할 승격 토큰 발급
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole().name());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 환자 매칭 요청 로직 (재사용을 위해 분리)
     */
    public MatchResponse requestPatientMatch(Doctor doctor, String patientEmail) {
        if (doctor.getUser().getEmail().equalsIgnoreCase(patientEmail)) {
            throw new BaseException(ErrorCode.INVALID_PATIENT);
        }

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (matchRepository.existsByDoctorIdAndPatientId(doctor.getId(), patient.getId())) {
            throw new BaseException(ErrorCode.ALREADY_MAPPED);
        }

        Match match = Match.builder()
                .doctor(doctor)
                .patient(patient)
                .status(MatchStatus.PENDING)
                .build();

        return MatchResponse.from(matchRepository.save(match));
    }

    private Hospital findOrCreateHospital(String name, String address, String phone) {
        String trimmedName = name.trim();
        String trimmedAddress = address.trim();

        return hospitalRepository.findByNameAndAddress(trimmedName, trimmedAddress)
                .orElseGet(() -> hospitalRepository.save(
                        Hospital.builder()
                                .name(trimmedName)
                                .address(trimmedAddress)
                                .phone(phone)
                                .build()
                ));
    }
}
