package com.remind.remind.service.examination;

import com.remind.remind.domain.examination.Examination;
import com.remind.remind.domain.examination.ExaminationType;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.examination.ExaminationCreateRequest;
import com.remind.remind.dto.examination.ExaminationResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.examination.ExaminationRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExaminationCommandService {

    private final ExaminationRepository examinationRepository;
    private final UserRepository userRepository;

    public ExaminationResponse createPHQ9(Long userId, ExaminationCreateRequest request) {
        validateScoreCount(request.getScores(), 9, 3);
        int totalScore = calculateTotalScore(request.getScores());
        String severity = getPHQ9Severity(totalScore);

        return saveExamination(userId, ExaminationType.PHQ_9, totalScore, severity);
    }

    public ExaminationResponse createGAD7(Long userId, ExaminationCreateRequest request) {
        validateScoreCount(request.getScores(), 7, 3);
        int totalScore = calculateTotalScore(request.getScores());
        String severity = getGAD7Severity(totalScore);

        return saveExamination(userId, ExaminationType.GAD_7, totalScore, severity);
    }

    public ExaminationResponse createStress(Long userId, ExaminationCreateRequest request) {
        validateScoreCount(request.getScores(), 10, 4);
        int totalScore = calculateTotalScore(request.getScores());
        String severity = getStressSeverity(totalScore);

        return saveExamination(userId, ExaminationType.STRESS, totalScore, severity);
    }

    public ExaminationResponse createBipolar(Long userId, ExaminationCreateRequest request) {
        validateScoreCount(request.getScores(), 13, 1);
        int totalScore = calculateTotalScore(request.getScores());
        String severity = getBipolarSeverity(totalScore);

        return saveExamination(userId, ExaminationType.BIPOLAR, totalScore, severity);
    }

    public ExaminationResponse createADHD(Long userId, ExaminationCreateRequest request) {
        validateScoreCount(request.getScores(), 18, 3);
        int totalScore = calculateTotalScore(request.getScores());
        String severity = getADHDSeverity(totalScore);

        return saveExamination(userId, ExaminationType.ADHD, totalScore, severity);
    }

    private ExaminationResponse saveExamination(Long userId, ExaminationType type, int totalScore, String severity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Examination examination = Examination.builder()
                .user(user)
                .type(type)
                .score(totalScore)
                .severity(severity)
                .build();

        return ExaminationResponse.from(examinationRepository.save(examination));
    }

    private void validateScoreCount(List<Integer> scores, int expectedCount, int maxScore) {
        if (scores.size() != expectedCount) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (scores.stream().anyMatch(score -> score < 0 || score > maxScore)) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private int calculateTotalScore(List<Integer> scores) {
        return scores.stream().mapToInt(Integer::intValue).sum();
    }

    private String getPHQ9Severity(int score) {
        if (score <= 4) return "정상";
        if (score <= 9) return "가벼운 우울";
        if (score <= 14) return "중간 정도의 우울";
        if (score <= 19) return "약간 심한 우울";
        return "심한 우울";
    }

    private String getGAD7Severity(int score) {
        if (score <= 4) return "정상";
        if (score <= 9) return "가벼운 불안";
        if (score <= 14) return "중간 정도의 불안";
        return "심한 불안";
    }

    private String getStressSeverity(int score) {
        if (score <= 13) return "정상";
        if (score <= 16) return "경도 스트레스";
        if (score <= 18) return "중등도 스트레스";
        return "심한 스트레스";
    }

    private String getBipolarSeverity(int score) {
        if (score >= 7) return "조울증 가능성 있음";
        return "정상";
    }

    private String getADHDSeverity(int score) {
        if (score >= 19) return "전문가 상담 필요";
        return "정상";
    }
}
