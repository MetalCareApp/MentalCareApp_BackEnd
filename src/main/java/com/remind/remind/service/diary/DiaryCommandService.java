package com.remind.remind.service.diary;

import com.remind.remind.domain.diary.Diary;
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

        long totalSleepMinutes = Duration.between(request.getSleepStartTime(), request.getSleepEndTime()).toMinutes();

        Diary diary = Diary.builder()
                .diaryDate(request.getDiaryDate())
                .title(request.getTitle())
                .content(request.getContent())
                .emotion(request.getEmotion())
                .sleepStartTime(request.getSleepStartTime())
                .sleepEndTime(request.getSleepEndTime())
                .totalSleepMinutes(totalSleepMinutes)
                .isMedicationTaken(request.isMedicationTaken())
                .user(user)
                .build();

        Diary savedDiary = diaryRepository.save(diary);
        return DiaryResponse.from(savedDiary);
    }

    public DiaryResponse updateDiary(Long diaryId, DiaryUpdateRequest request, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BaseException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.DIARY_ACCESS_DENIED);
        }

        // 수면 시간 재계산 여부 확인: 시작 시간이나 종료 시간 중 하나라도 새로 들어왔을 때만 재계산
        LocalDateTime start = diary.getSleepStartTime();
        LocalDateTime end = diary.getSleepEndTime();
        Long totalSleepMinutes = diary.getTotalSleepMinutes();

        if (request.getSleepStartTime() != null || request.getSleepEndTime() != null) {
            start = request.getSleepStartTime() != null ? request.getSleepStartTime() : diary.getSleepStartTime();
            end = request.getSleepEndTime() != null ? request.getSleepEndTime() : diary.getSleepEndTime();
            
            if (start != null && end != null) {
                totalSleepMinutes = Duration.between(start, end).toMinutes();
            }
        }

        diary.update(
                request.getDiaryDate() != null ? request.getDiaryDate() : diary.getDiaryDate(),
                request.getTitle() != null ? request.getTitle() : diary.getTitle(),
                request.getContent() != null ? request.getContent() : diary.getContent(),
                request.getEmotion() != null ? request.getEmotion() : diary.getEmotion(),
                start,
                end,
                totalSleepMinutes,
                request.getMedicationTaken() != null ? request.getMedicationTaken() : diary.isMedicationTaken()
        );

        return DiaryResponse.from(diary);
    }
}
