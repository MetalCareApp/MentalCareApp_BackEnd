package com.remind.remind.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "matches")
@SQLDelete(sql = "UPDATE matches SET deleted_at = CURRENT_TIMESTAMP WHERE match_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User patient; // 환자(일반 유저)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status; // PENDING, ACCEPT, REJECT

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Match(Doctor doctor, User patient, MatchStatus status) {
        this.doctor = doctor;
        this.patient = patient;
        this.status = status;
    }

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }
}
