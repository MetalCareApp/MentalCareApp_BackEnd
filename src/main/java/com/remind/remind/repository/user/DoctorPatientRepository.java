package com.remind.remind.repository.user;

import com.remind.remind.domain.user.DoctorPatient;
import com.remind.remind.domain.user.MappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorPatientRepository extends JpaRepository<DoctorPatient, Long> {
    List<DoctorPatient> findAllByDoctorId(Long doctorId);
    List<DoctorPatient> findAllByPatientId(Long patientId);
    List<DoctorPatient> findAllByDoctorIdAndStatus(Long doctorId, MappingStatus status);
    List<DoctorPatient> findAllByPatientIdAndStatus(Long patientId, MappingStatus status);
    boolean existsByDoctorIdAndPatientId(Long doctorId, Long patientId);
}
