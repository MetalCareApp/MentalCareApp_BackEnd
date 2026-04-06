package com.remind.remind.domain.diary;

import com.remind.remind.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "diaries")
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private java.time.LocalDate diaryDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Emotion emotion;

    @Column(nullable = false)
    private LocalDateTime sleepStartTime;

    @Column(nullable = false)
    private LocalDateTime sleepEndTime;

    @Column(nullable = false)
    private Long totalSleepMinutes;

    @Column(nullable = false)
    private boolean isMedicationTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Diary(String title, java.time.LocalDate diaryDate, String content, Emotion emotion, LocalDateTime sleepStartTime, 
                 LocalDateTime sleepEndTime, Long totalSleepMinutes, boolean isMedicationTaken, User user) {
        this.title = title;
        this.diaryDate = diaryDate;
        this.content = content;
        this.emotion = emotion;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.totalSleepMinutes = totalSleepMinutes;
        this.isMedicationTaken = isMedicationTaken;
        this.user = user;
    }

    public void update(String title, java.time.LocalDate diaryDate, String content, Emotion emotion, 
                       LocalDateTime sleepStartTime, LocalDateTime sleepEndTime, boolean isMedicationTaken) {
        this.title = title;
        this.diaryDate = diaryDate;
        this.content = content;
        this.emotion = emotion;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.isMedicationTaken = isMedicationTaken;
        this.totalSleepMinutes = java.time.Duration.between(sleepStartTime, sleepEndTime).toMinutes();
    }
}
