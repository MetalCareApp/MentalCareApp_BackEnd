package com.remind.remind.repository.hospital;

import com.remind.remind.domain.hospital.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByNameAndAddress(String name, String address);
}
