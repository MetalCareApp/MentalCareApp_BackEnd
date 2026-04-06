package com.remind.remind.service.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.user.User;
import com.remind.remind.dto.diary.DiaryResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.diary.DiaryRepository;
import com.remind.remind.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryQueryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public DiaryResponse getDiary(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Diary diary = diaryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR)); // TODO: DIARY_NOT_FOUND 에러코드 추가 예정

        return DiaryResponse.from(diary);
    }

    public List<DiaryResponse> getMonthlyDiaries(String yyyymm, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        YearMonth yearMonth = parseYearMonth(yyyymm);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        return diaryRepository.findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(user, start, end)
                .stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    private YearMonth parseYearMonth(String yyyymm) {
        try {
            // "2026-04" 형식 처리
            if (yyyymm.contains("-")) {
                return YearMonth.parse(yyyymm, DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            // "202604" 형식 처리
            return YearMonth.parse(yyyymm, DateTimeFormatter.ofPattern("yyyyMM"));
        } catch (Exception e) {
            // 파싱 실패 시 500이 아닌 400 Bad Request를 던지도록 수정
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
