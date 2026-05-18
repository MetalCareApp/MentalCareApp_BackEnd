package com.remind.remind.repository.user;

import com.remind.remind.domain.user.Match;
import com.remind.remind.domain.user.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByDoctorId(Long doctorId);
    List<Match> findAllByPatientId(Long patientId);
    List<Match> findAllByDoctorIdAndStatus(Long doctorId, MatchStatus status);
    List<Match> findAllByPatientIdAndStatus(Long patientId, MatchStatus status);
    Optional<Match> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
    boolean existsByPatientIdAndStatus(Long patientId, MatchStatus status);
}
