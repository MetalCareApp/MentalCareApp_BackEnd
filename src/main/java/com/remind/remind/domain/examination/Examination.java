package com.remind.remind.domain.examination;

import com.remind.remind.domain.user.User;
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
@Table(name = "examinations")
@SQLDelete(sql = "UPDATE examinations SET deleted_at = CURRENT_TIMESTAMP WHERE examination_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Examination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "examination_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExaminationType type;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(nullable = false, length = 100)
    private String severity;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Examination(User user, ExaminationType type, Integer score, String severity) {
        this.user = user;
        this.type = type;
        this.score = score;
        this.severity = severity;
    }
}
