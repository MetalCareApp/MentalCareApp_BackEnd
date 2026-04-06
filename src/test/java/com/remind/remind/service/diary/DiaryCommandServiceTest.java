package com.remind.remind.service.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.diary.Emotion;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.diary.DiaryCreateRequest;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.repository.diary.DiaryRepository;
import com.remind.remind.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DiaryCommandServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiaryCommandService diaryCommandService;

    @Test
    @DisplayName("일기 생성 성공")
    void createDiary_Success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .username("test@gmail.com")
                .nickname("tester")
                .role(Role.USER)
                .build();

        LocalDate diaryDate = LocalDate.of(2024, 1, 1);
        LocalDateTime sleepStart = LocalDateTime.of(2024, 1, 1, 23, 0);
        LocalDateTime sleepEnd = LocalDateTime.of(2024, 1, 2, 7, 0); // 8 hours = 480 minutes

        DiaryCreateRequest request = new DiaryCreateRequest(diaryDate, "제목", "내용", Emotion.GOOD, sleepStart, sleepEnd, true);
        Diary diary = Diary.builder()
                .diaryDate(request.getDiaryDate())
                .title(request.getTitle())
                .content(request.getContent())
                .emotion(request.getEmotion())
                .sleepStartTime(request.getSleepStartTime())
                .sleepEndTime(request.getSleepEndTime())
                .totalSleepMinutes(480L)
                .isMedicationTaken(true)
                .user(user)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(diaryRepository.save(any(Diary.class))).willReturn(diary);

        // when
        DiaryResponse response = diaryCommandService.createDiary(request, userId);

        // then
        assertThat(response.getDiaryDate()).isEqualTo(diaryDate);
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getEmotion()).isEqualTo(request.getEmotion());
        assertThat(response.getSleepStartTime()).isEqualTo(sleepStart);
        assertThat(response.getSleepEndTime()).isEqualTo(sleepEnd);
        assertThat(response.getTotalSleepMinutes()).isEqualTo(480L);
        assertThat(response.isMedicationTaken()).isTrue();
    }
}
