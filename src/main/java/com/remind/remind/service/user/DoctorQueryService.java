package com.remind.remind.service.user;

import com.remind.remind.domain.user.Doctor;
import com.remind.remind.domain.user.Match;
import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.user.DoctorRepository;
import com.remind.remind.repository.user.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.remind.remind.dto.user.MatchResponse;
import java.util.List;
import java.util.stream.Collectors;

import com.remind.remind.dto.user.MatchDetailResponse;

import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.user.PatientSearchResponse;
import com.remind.remind.repository.user.UserRepository;
import com.remind.remind.dto.user.MatchedPatientResponse;

import com.remind.remind.dto.user.MatchedPatientDetailResponse;
import com.remind.remind.repository.report.ReportRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    /**
     * 의사용 환자 상세 정보 조회 (환자 정보 + 리포트 목록)
     */
    public MatchedPatientDetailResponse getMatchedPatientDetail(Long matchId, Long doctorUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        // 권한 체크: 해당 의사의 매칭인지 확인
        if (!match.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // 상태 체크: ACCEPTED 상태인 경우에만 상세 정보 제공
        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 해당 매칭의 리포트 목록 조회
        List<MatchedPatientDetailResponse.ReportSummary> reportSummaries = reportRepository.findAllByMatchIdOrderByCreatedAtDesc(matchId).stream()
                .map(report -> MatchedPatientDetailResponse.ReportSummary.builder()
                        .id(report.getId())
                        .createdAt(report.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return MatchedPatientDetailResponse.builder()
                .userId(match.getPatient().getId())
                .name(match.getPatient().getName())
                .email(match.getPatient().getEmail())
                .matchCreatedAt(match.getCreatedAt())
                .reports(reportSummaries)
                .build();
    }

    /**
     * 의사의 관리 환자(수락 완료) 목록 조회
     */
    public List<MatchedPatientResponse> getAcceptedPatients(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return matchRepository.findAllByDoctorIdAndStatus(doctor.getId(), MatchStatus.ACCEPTED).stream()
                .map(MatchedPatientResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 환자 검색 (의사용)
     * 1. 이메일 포함 검색
     * 2. 이미 어느 의사와든 ACCEPTED인 환자는 제외
     * 3. 현재 의사와 PENDING 상태인지 여부 반환
     */
    public List<PatientSearchResponse> searchPatients(String email, Long currentUserId) {
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        List<User> patients = userRepository.findByEmailContainingAndRole(email, Role.USER);

        return patients.stream()
                // 1. 어느 의사와든 ACCEPTED 상태인 환자는 무조건 제외 (이미 매칭 확정)
                .filter(patient -> !matchRepository.existsByPatientIdAndStatus(patient.getId(), MatchStatus.ACCEPTED))
                .map(patient -> {
                    // 2. 현재 의사(나)와의 매칭 상태 확인 (PENDING 또는 null)
                    // 다른 의사가 PENDING 요청을 보냈더라도 검색 결과에는 나타나며, 내 상태는 null로 나옵니다.
                    MatchStatus status = matchRepository.findByDoctorIdAndPatientId(doctor.getId(), patient.getId())
                            .map(Match::getStatus)
                            .orElse(null);
                    
                    return PatientSearchResponse.of(patient, status);
                })
                .collect(Collectors.toList());
    }

    /**
     * 매칭 상세 정보 조회 (환자용)
     */
    public MatchDetailResponse getMatchDetail(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BaseException(ErrorCode.MATCH_NOT_FOUND));

        // 권한 체크: 해당 환자의 매칭인지 확인
        if (!match.getPatient().getId().equals(userId)) {
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // 상태 체크: ACCEPTED 상태인 경우에만 상세 정보 제공 (요구사항)
        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE); // 혹은 적절한 에러코드
        }

        return MatchDetailResponse.from(match);
    }

    /**
     * 환자에게 온 매칭 요청 목록 조회 (대기 중인 요청만)
     */
    public List<MatchResponse> getPatientRequests(Long userId) {
        return matchRepository.findAllByPatientIdAndStatus(userId, MatchStatus.PENDING).stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 환자의 담당 의사(수락 완료) 목록 조회
     */
    public List<MatchResponse> getAcceptedDoctors(Long userId) {
        return matchRepository.findAllByPatientIdAndStatus(userId, MatchStatus.ACCEPTED).stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
    }
}
