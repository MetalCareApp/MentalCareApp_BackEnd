package com.remind.remind.dto.diary;

import com.remind.remind.domain.diary.Emotion;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class DiaryUpdateRequest {

    private LocalDate diaryDate;
    private String content;
    private Emotion emotion;
    private LocalDateTime sleepStartTime;
    private LocalDateTime sleepEndTime;
    private Boolean medicationTaken;
    private String medicationReaction;

    public DiaryUpdateRequest(LocalDate diaryDate, String content, Emotion emotion, 
                              LocalDateTime sleepStartTime, LocalDateTime sleepEndTime, Boolean medicationTaken, String medicationReaction) {
        this.diaryDate = diaryDate;
        this.content = content;
        this.emotion = emotion;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.medicationTaken = medicationTaken;
        this.medicationReaction = medicationReaction;
    }
}
