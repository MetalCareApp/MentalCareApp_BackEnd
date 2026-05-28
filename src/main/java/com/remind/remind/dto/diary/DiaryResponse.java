package com.remind.remind.dto.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.diary.Emotion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryResponse {

    private Long id;
    private LocalDate diaryDate;
    private String content;
    private Emotion emotion;
    private int emotionScore;
    private LocalDateTime sleepStartTime;
    private LocalDateTime sleepEndTime;
    private Long totalSleepMinutes;
    private boolean medicationTaken;
    private String medicationReaction;
    private boolean externalStress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .diaryDate(diary.getDiaryDate())
                .content(diary.getContent())
                .emotion(diary.getEmotion())
                .emotionScore(diary.getEmotion().getScore())
                .sleepStartTime(diary.getSleepStartTime())
                .sleepEndTime(diary.getSleepEndTime())
                .totalSleepMinutes(diary.getTotalSleepMinutes())
                .medicationTaken(diary.isMedicationTaken())
                .medicationReaction(diary.getMedicationReaction())
                .externalStress(diary.isExternalStress())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
