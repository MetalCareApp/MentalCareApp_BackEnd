package com.remind.remind.repository.hospital;

import com.remind.remind.domain.hospital.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByApiId(String apiId);
    Optional<Hospital> findByNameAndAddress(String name, String address);
    List<Hospital> findByNameAndPhone(String name, String phone);

    // [수정] 정신과 전문의가 1명이라도 있는 병원만 검색 (정제된 데이터만 노출)
    List<Hospital> findByNameContainingAndSpecialistCountGreaterThan(String name, Integer specialistCount);

    List<Hospital> findByAddressContainingAndSpecialistCountGreaterThan(String address, Integer specialistCount);

    List<Hospital> findByNameContainingAndAddressContainingAndSpecialistCountGreaterThan(String name, String address, Integer specialistCount);

    List<Hospital> findAllBySpecialistCountGreaterThan(Integer specialistCount);

    // 아직 동기화되지 않은(의사 수가 0인) 병원 목록 조회
    List<Hospital> findAllBySpecialistCountAndGeneralDoctorCount(Integer specialistCount, Integer generalDoctorCount);

    // [개선] 동기화가 필요한 병원 조회 (날것의 데이터 우선)
    // 1. 개점일과 전문의 수가 모두 없는 '순수 날것' 우선 시도
    // 2. 개점일이나 전문의 수 중 하나라도 없는 '미완성' 데이터 시도
    // 3. 단, 업데이트 시도 후 1시간이 지난 데이터만 대상으로 하여 트래픽 낭비 방지
    @org.springframework.data.jpa.repository.Query("SELECT h FROM Hospital h " +
            "WHERE (h.openingDate IS NULL OR h.specialistCount IS NULL) " +
            "AND h.updatedAt < :dateTime " +
            "ORDER BY (CASE WHEN h.openingDate IS NULL AND h.specialistCount IS NULL THEN 0 ELSE 1 END) ASC, h.updatedAt ASC")
    List<Hospital> findAllToSync(java.time.LocalDateTime dateTime);

    // [추가] API ID 중복 체크 (Soft Delete 포함)
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM hospitals WHERE api_id = :apiId LIMIT 1", nativeQuery = true)
    java.util.Optional<Hospital> findByApiIdIncludeDeleted(String apiId);

    // [수정] 개점일은 존재하지만 의사가 0명임이 "확인"된 병원만 일괄 Soft Delete
    // (NULL인 경우는 아직 확인되지 않은 것이므로 삭제 대상에서 제외하여 다음 회차에 재시도 가능케 함)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Hospital h SET h.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE h.openingDate IS NOT NULL " +
            "AND h.specialistCount = 0 " +
            "AND h.generalDoctorCount = 0 " +
            "AND h.deletedAt IS NULL")
    int softDeleteNoPsychiatryHospitals();
}
