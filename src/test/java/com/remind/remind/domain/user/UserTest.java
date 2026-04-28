package com.remind.remind.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("일반 유저의 역할 확인 메서드 검증")
    void userRole_IsUser() {
        // given
        User user = User.builder()
                .role(Role.USER)
                .build();

        // when & then
        assertThat(user.isUser()).isTrue();
        assertThat(user.isDoctor()).isFalse();
    }

    @Test
    @DisplayName("의사 유저의 역할 확인 메서드 검증")
    void doctorRole_IsDoctor() {
        // given
        User user = User.builder()
                .role(Role.DOCTOR)
                .build();

        // when & then
        assertThat(user.isDoctor()).isTrue();
        assertThat(user.isUser()).isFalse();
    }

    @Test
    @DisplayName("의사 승격 시 역할이 DOCTOR로 변경되는지 검증")
    void promoteToDoctor_Success() {
        // given
        User user = User.builder()
                .role(Role.USER)
                .build();

        // when
        user.promoteToDoctor();

        // then
        assertThat(user.getRole()).isEqualTo(Role.DOCTOR);
        assertThat(user.isDoctor()).isTrue();
    }
}
