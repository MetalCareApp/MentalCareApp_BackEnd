package com.remind.remind.service.user;

import com.remind.remind.domain.user.Doctor;
import com.remind.remind.domain.user.MappingStatus;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.user.DoctorRepository;
import com.remind.remind.repository.user.DoctorPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.remind.remind.dto.user.DoctorPatientResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;
    private final DoctorPatientRepository doctorPatientRepository;

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    /**
     * 환자에게 온 매칭 요청 목록 조회
     */
    public List<DoctorPatientResponse> getPatientRequests(Long userId) {
        return doctorPatientRepository.findAllByPatientId(userId).stream()
                .map(DoctorPatientResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 의사의 관리 환자(수락 완료) 목록 조회
     */
    public List<DoctorPatientResponse> getAcceptedPatients(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return doctorPatientRepository.findAllByDoctorId(doctor.getId()).stream()
                .filter(m -> m.getStatus() == MappingStatus.ACCEPTED)
                .map(DoctorPatientResponse::from)
                .collect(Collectors.toList());
    }
}
