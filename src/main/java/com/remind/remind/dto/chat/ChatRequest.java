package com.remind.remind.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRequest {
    @NotBlank(message = "대화 내용은 필수입니다.")
    private String question;
}
