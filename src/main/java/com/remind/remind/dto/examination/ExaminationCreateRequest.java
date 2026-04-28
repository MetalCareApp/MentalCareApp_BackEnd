package com.remind.remind.dto.examination;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExaminationCreateRequest {
    
    @NotEmpty(message = "각 문항의 점수는 필수입니다.")
    private List<Integer> scores;
}
