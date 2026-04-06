package com.remind.remind.domain.diary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Emotion {
    EXTREMELY_GOOD("매우 좋음", 5),
    GOOD("좋음", 4),
    NORMAL("보통", 3),
    BAD("나쁨", 2),
    EXTREMELY_BAD("매우 나쁨", 1);

    private final String description;
    private final int score;
}
