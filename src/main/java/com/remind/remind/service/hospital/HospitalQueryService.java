package com.remind.remind.service.hospital;

import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.domain.hospital.HospitalLike;
import com.remind.remind.dto.hospital.HospitalResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.hospital.HospitalLikeRepository;
import com.remind.remind.repository.hospital.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalQueryService {

    private final HospitalRepository hospitalRepository;
    private final HospitalLikeRepository hospitalLikeRepository;

    /**
     * 병원 전체 목록 조회
     */
    public List<HospitalResponse> getAllHospitals(Long userId) {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return hospitals.stream()
                .map(hospital -> {
                    boolean isLiked = hospitalLikeRepository.existsByUserIdAndHospitalId(userId, hospital.getId());
                    return HospitalResponse.from(hospital, isLiked);
                })
                .collect(Collectors.toList());
    }

    /**
     * 병원 상세 정보 조회
     */
    public HospitalResponse getHospitalDetail(Long hospitalId, Long userId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BaseException(ErrorCode.HOSPITAL_NOT_FOUND));
        
        boolean isLiked = hospitalLikeRepository.existsByUserIdAndHospitalId(userId, hospital.getId());
        return HospitalResponse.from(hospital, isLiked);
    }

    /**
     * 찜한 병원 목록 조회
     */
    public List<HospitalResponse> getLikedHospitals(Long userId) {
        List<HospitalLike> likes = hospitalLikeRepository.findAllByUserId(userId);
        return likes.stream()
                .map(like -> HospitalResponse.from(like.getHospital(), true))
                .collect(Collectors.toList());
    }
}
