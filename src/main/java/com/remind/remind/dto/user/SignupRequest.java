package com.remind.remind.dto.user;

import com.remind.remind.domain.user.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "구글 ID 토큰은 필수입니다.")
    private String idToken;

    private String name;

    private String phone;

    private LocalDate birthDate;

    private Gender gender;
}
