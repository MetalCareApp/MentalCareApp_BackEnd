package com.remind.remind.dto.diary;

import com.remind.remind.domain.diary.Emotion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class DiaryCreateRequest {

    @NotNull(message = "작성 날짜는 필수입니다.")
    private LocalDate diaryDate;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "감정은 필수입니다.")
    private Emotion emotion;

    @NotNull(message = "수면 시작 시간은 필수입니다.")
    private LocalDateTime sleepStartTime;

    @NotNull(message = "수면 종료 시간은 필수입니다.")
    private LocalDateTime sleepEndTime;

    private boolean medicationTaken;

    public DiaryCreateRequest(LocalDate diaryDate, String title, String content, Emotion emotion, 
                              LocalDateTime sleepStartTime, LocalDateTime sleepEndTime, boolean medicationTaken) {
        this.diaryDate = diaryDate;
        this.title = title;
        this.content = content;
        this.emotion = emotion;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.medicationTaken = medicationTaken;
    }
}
