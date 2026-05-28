package com.remind.remind.service.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.diary.Emotion;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.diary.DiaryCreateRequest;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.dto.diary.DiaryUpdateRequest;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.diary.DiaryRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryCommandService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public DiaryResponse createDiary(DiaryCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startTime = request.getSleepStartTime();
        LocalDateTime endTime = request.getSleepEndTime();

        // 수면 종료 시간이 시작 시간보다 이른 경우 (날짜가 바뀐 경우) 종료 시간을 하루 뒤로 설정
        if (endTime.isBefore(startTime)) {
            endTime = endTime.plusDays(1);
        }

        long totalSleepMinutes = Duration.between(startTime, endTime).toMinutes();

        Diary diary = Diary.builder()
                .diaryDate(request.getDiaryDate())
                .content(request.getContent())
                .emotion(request.getEmotion())
                .sleepStartTime(startTime)
                .sleepEndTime(endTime)
                .totalSleepMinutes(totalSleepMinutes)
                .isMedicationTaken(request.isMedicationTaken())
                .medicationReaction(request.getMedicationReaction())
                .isExternalStress(request.isExternalStress())
                .user(user)
                .build();

        return DiaryResponse.from(diaryRepository.save(diary));
    }

    public DiaryResponse updateDiary(Long diaryId, DiaryUpdateRequest request, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BaseException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        // 값 병합 (null이 아닌 값만 우선 적용)
        LocalDate diaryDate = request.getDiaryDate() != null ? request.getDiaryDate() : diary.getDiaryDate();
        String content = request.getContent() != null ? request.getContent() : diary.getContent();
        Emotion emotion = request.getEmotion() != null ? request.getEmotion() : diary.getEmotion();
        LocalDateTime start = request.getSleepStartTime() != null ? request.getSleepStartTime() : diary.getSleepStartTime();
        LocalDateTime end = request.getSleepEndTime() != null ? request.getSleepEndTime() : diary.getSleepEndTime();
        boolean medication = request.getMedicationTaken() != null ? request.getMedicationTaken() : diary.isMedicationTaken();
        String reaction = request.getMedicationReaction() != null ? request.getMedicationReaction() : diary.getMedicationReaction();
        boolean externalStress = request.getExternalStress() != null ? request.getExternalStress() : diary.isExternalStress();

        // 엔티티 내부에서 수면 시간 재계산 처리
        diary.update(diaryDate, content, emotion, start, end, medication, reaction, externalStress);

        return DiaryResponse.from(diary);
    }

    public void deleteDiary(Long diaryId, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BaseException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        diaryRepository.delete(diary);
    }
}
