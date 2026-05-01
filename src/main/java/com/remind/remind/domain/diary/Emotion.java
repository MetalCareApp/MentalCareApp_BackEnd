package com.remind.remind.domain.diary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Emotion {
    GOOD("좋음", 5),
    NORMAL("보통", 4),
    DEPRESSED("우울", 3),
    ANXIOUS("불안", 2),
    ANGRY("화남", 1),
    EXHAUSTED("지침", 0);

    private final String description;
    private final int score;
}
