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
import org.springframework.web.multipart.MultipartFile;

import com.remind.remind.dto.user.MatchRequest;
import com.remind.remind.dto.user.MatchResponse;
import com.remind.remind.service.common.S3Service;
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
    private final S3Service s3Service;

    /**
     * 의사가 환자에게 매칭 요청 전송
     */
    public MatchResponse requestMatching(Long currentUserId, MatchRequest request) {
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return requestPatientMatch(doctor, request.getPatientEmail().trim());
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

        // 의사가 의사에게 요청하는 것 방지
        if (patient.isDoctor()) {
            throw new BaseException(ErrorCode.CANNOT_MATCH_DOCTOR); // "상대방이 의사 계정입니다. 의사 계정은 환자로 매칭할 수 없습니다."
        }

        Match match = matchRepository.findByDoctorIdAndPatientId(doctor.getId(), patient.getId())
                .map(existingMatch -> {
                    MatchStatus status = existingMatch.getStatus();
                    if (status == MatchStatus.PENDING || status == MatchStatus.ACCEPTED) {
                        throw new BaseException(ErrorCode.ALREADY_MAPPED);
                    }
                    // REJECTED 상태인 경우 다시 PENDING으로 변경하여 요청
                    existingMatch.updateStatus(MatchStatus.PENDING);
                    return existingMatch;
                })
                .orElseGet(() -> matchRepository.save(Match.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .status(MatchStatus.PENDING)
                        .build()));

        return MatchResponse.from(match);
    }

    /**
     * 매칭 상태 변경 (수락/거절)
     * 환자는 1인 1매칭만 가능합니다.
     */
    public MatchResponse updateMatchStatus(Long matchId, MatchStatus status, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        // 권한 체크: 수락/거절은 해당 환자만 가능
        if (!match.getPatient().getId().equals(currentUserId)) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // 수락(ACCEPTED) 시도 시 중복 매칭 체크
        if (status == MatchStatus.ACCEPTED) {
            boolean alreadyMatched = matchRepository.existsByPatientIdAndStatus(currentUserId, MatchStatus.ACCEPTED);
            if (alreadyMatched) {
                throw new BaseException(ErrorCode.ALREADY_MAPPED); // "이미 매칭된 병원이 있습니다."
            }
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
     * 의사 회원가입 신청 (수동 승인 대기 상태로 저장)
     */
    public void signup(Long currentUserId, DoctorSignupRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (user.isDoctor()) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 이미 신청 중인지 확인
        if (doctorRepository.findByUserId(currentUserId).isPresent()) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        Hospital hospital = findOrCreateHospital(request.getHospitalName(), request.getHospitalPhone());

        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .patientCount(0)
                .status(MatchStatus.PENDING)
                .build();

        doctorRepository.save(doctor);
    }

    /**
     * 의사 회원가입 신청 v2 (S3 사진 업로드 포함)
     */
    public void signupV2(Long currentUserId, DoctorSignupRequest request, MultipartFile certificationImage) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (user.isDoctor() || doctorRepository.findByUserId(currentUserId).isPresent()) {
            throw new BaseException(ErrorCode.ALREADY_REGISTERED);
        }

        // 1. S3 이미지 업로드
        String imageUrl = null;
        if (certificationImage != null && !certificationImage.isEmpty()) {
            imageUrl = s3Service.uploadFile(certificationImage, "doctors/certifications");
        }

        // 2. 병원 데이터 조회/생성
        Hospital hospital = findOrCreateHospital(request.getHospitalName(), request.getHospitalPhone());

        // 3. Doctor 프로필 생성 (이미지 URL 포함)
        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .patientCount(0)
                .status(MatchStatus.PENDING)
                .certificationImageUrl(imageUrl)
                .build();

        doctorRepository.save(doctor);
    }

    private Hospital findOrCreateHospital(String name, String phone) {
        String trimmedName = name.trim();

        return hospitalRepository.findByNameAndPhone(trimmedName, phone)
                .stream().findFirst()
                .orElseGet(() -> hospitalRepository.save(
                        Hospital.builder()
                                .name(trimmedName)
                                .phone(phone)
                                .apiId("PENDING_" + System.currentTimeMillis())
                                .address("주소 확인 필요")
                                .build()
                ));
    }
}
