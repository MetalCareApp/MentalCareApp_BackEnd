package com.remind.remind.repository.user;

import com.remind.remind.domain.user.Mapping;
import com.remind.remind.domain.user.MappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MappingRepository extends JpaRepository<Mapping, Long> {
    List<Mapping> findAllByDoctorId(Long doctorId);
    List<Mapping> findAllByPatientId(Long patientId);
    List<Mapping> findAllByDoctorIdAndStatus(Long doctorId, MappingStatus status);
    List<Mapping> findAllByPatientIdAndStatus(Long patientId, MappingStatus status);
    boolean existsByDoctorIdAndPatientId(Long doctorId, Long patientId);
}
