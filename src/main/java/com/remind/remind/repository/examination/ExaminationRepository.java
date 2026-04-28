package com.remind.remind.repository.examination;

import com.remind.remind.domain.examination.Examination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
}
