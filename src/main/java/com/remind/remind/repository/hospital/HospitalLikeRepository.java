package com.remind.remind.repository.hospital;

import com.remind.remind.domain.hospital.HospitalLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HospitalLikeRepository extends JpaRepository<HospitalLike, Long> {
    List<HospitalLike> findAllByUserId(Long userId);
    Optional<HospitalLike> findByUserIdAndHospitalId(Long userId, Long hospitalId);
    boolean existsByUserIdAndHospitalId(Long userId, Long hospitalId);
    void deleteByUserIdAndHospitalId(Long userId, Long hospitalId);
}
