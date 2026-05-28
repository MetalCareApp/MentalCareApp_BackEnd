package com.remind.remind.domain.diary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Emotion {
    GREAT("매우좋음", 5),
    GOOD("좋음", 4),
    NORMAL("보통", 3),
    BAD("나쁨", 2),
    VERY_BAD("매우나쁨", 1);

    private final String description;
    private final int score;
}