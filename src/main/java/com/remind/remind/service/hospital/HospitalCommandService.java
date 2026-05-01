package com.remind.remind.service.hospital;

import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.domain.hospital.HospitalLike;
import com.remind.remind.domain.user.User;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.hospital.HospitalLikeRepository;
import com.remind.remind.repository.hospital.HospitalRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalCommandService {

    private final HospitalRepository hospitalRepository;
    private final HospitalLikeRepository hospitalLikeRepository;
    private final UserRepository userRepository;

    /**
     * 병원 찜하기
     */
    public void likeHospital(Long userId, Long hospitalId) {
        if (hospitalLikeRepository.existsByUserIdAndHospitalId(userId, hospitalId)) {
            return; // 이미 찜한 경우 무시
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BaseException(ErrorCode.HOSPITAL_NOT_FOUND));

        HospitalLike like = HospitalLike.builder()
                .user(user)
                .hospital(hospital)
                .build();

        hospitalLikeRepository.save(like);
    }

    /**
     * 병원 찜 취소
     */
    public void unlikeHospital(Long userId, Long hospitalId) {
        hospitalLikeRepository.deleteByUserIdAndHospitalId(userId, hospitalId);
    }
}
