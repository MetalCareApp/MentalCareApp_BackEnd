package com.remind.remind.repository.diary;

import com.remind.remind.domain.diary.Diary;
import com.remind.remind.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByIdAndUser(Long id, User user);
    List<Diary> findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(User user, LocalDate start, LocalDate end);
}
