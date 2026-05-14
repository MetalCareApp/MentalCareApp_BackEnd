package com.remind.remind.service.user;

import com.remind.remind.domain.user.Doctor;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;
    private final MatchRepository matchRepository;

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    /**
     * 환자에게 온 매칭 요청 목록 조회
     */
    public List<MatchResponse> getPatientRequests(Long userId) {
        return matchRepository.findAllByPatientId(userId).stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 의사의 관리 환자(수락 완료) 목록 조회
     */
    public List<MatchResponse> getAcceptedPatients(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return matchRepository.findAllByDoctorIdAndStatus(doctor.getId(), MatchStatus.ACCEPTED).stream()
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
