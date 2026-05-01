package com.remind.remind.repository.hospital;

import com.remind.remind.domain.hospital.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByNameAndAddress(String name, String address);

    // 이름 포함 검색
    List<Hospital> findByNameContaining(String name);

    // 주소(지역) 포함 검색
    List<Hospital> findByAddressContaining(String address);

    // 이름과 주소 모두 포함 검색
    List<Hospital> findByNameContainingAndAddressContaining(String name, String address);
}
