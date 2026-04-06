package com.remind.remind.service.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.diary.Emotion;
import com.remind.remind.domain.user.Role;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.diary.DiaryCreateRequest;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.dto.diary.DiaryUpdateRequest;
import com.remind.remind.repository.diary.DiaryRepository;
import com.remind.remind.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("일기 수정 성공")
    void updateDiary_Success() {
        // given
        Long userId = 1L;
        Long diaryId = 1L;

        User user = User.builder()
                .username("test@gmail.com")
                .nickname("tester")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Diary diary = Diary.builder()
                .diaryDate(LocalDate.of(2024, 1, 1))
                .title("원래 제목")
                .content("원래 내용")
                .emotion(Emotion.GOOD)
                .sleepStartTime(LocalDateTime.of(2024, 1, 1, 23, 0))
                .sleepEndTime(LocalDateTime.of(2024, 1, 2, 7, 0))
                .totalSleepMinutes(480L)
                .isMedicationTaken(true)
                .user(user)
                .build();

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.of(2024, 1, 2),
                "수정된 제목",
                "수정된 내용",
                Emotion.EXTREMELY_GOOD,
                LocalDateTime.of(2024, 1, 2, 22, 0),
                LocalDateTime.of(2024, 1, 3, 8, 0), // 10 hours = 600 minutes
                false
        );

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        DiaryResponse response = diaryCommandService.updateDiary(diaryId, request, userId);

        // then
        assertThat(response.getDiaryDate()).isEqualTo(request.getDiaryDate());
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getEmotion()).isEqualTo(request.getEmotion());
        assertThat(response.getSleepStartTime()).isEqualTo(request.getSleepStartTime());
        assertThat(response.getSleepEndTime()).isEqualTo(request.getSleepEndTime());
        assertThat(response.getTotalSleepMinutes()).isEqualTo(600L);
        assertThat(response.isMedicationTaken()).isFalse();
    }

    @Test
    @DisplayName("일기 삭제 성공")
    void deleteDiary_Success() {
        // given
        Long userId = 1L;
        Long diaryId = 1L;

        User user = User.builder()
                .username("test@gmail.com")
                .nickname("tester")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Diary diary = Diary.builder()
                .user(user)
                .build();

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        diaryCommandService.deleteDiary(diaryId, userId);

        // then
        verify(diaryRepository, times(1)).delete(diary);
    }
}
