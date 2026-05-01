package com.remind.remind.service.examination;

import com.remind.remind.dto.examination.ExaminationResponse;
import com.remind.remind.repository.examination.ExaminationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExaminationQueryService {

    private final ExaminationRepository examinationRepository;

    public List<ExaminationResponse> getMyExaminations(Long userId) {
        return examinationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ExaminationResponse::from)
                .collect(Collectors.toList());
    }
}
