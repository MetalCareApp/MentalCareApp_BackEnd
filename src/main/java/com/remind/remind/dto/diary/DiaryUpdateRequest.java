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
    private String title;
    private String content;
    private Emotion emotion;
    private LocalDateTime sleepStartTime;
    private LocalDateTime sleepEndTime;
    private Boolean medicationTaken;

    public DiaryUpdateRequest(LocalDate diaryDate, String title, String content, Emotion emotion, 
                              LocalDateTime sleepStartTime, LocalDateTime sleepEndTime, Boolean medicationTaken) {
        this.diaryDate = diaryDate;
        this.title = title;
        this.content = content;
        this.emotion = emotion;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.medicationTaken = medicationTaken;
    }
}
