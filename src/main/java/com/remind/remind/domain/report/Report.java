package com.remind.remind.domain.report;

import com.remind.remind.domain.user.Match;
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
@Table(name = "reports")
@SQLDelete(sql = "UPDATE reports SET deleted_at = CURRENT_TIMESTAMP WHERE report_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private java.time.LocalDate startDate;

    @Column(nullable = false)
    private java.time.LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer totalScore;

    private String riskLevel;

    private String phq9Slots;

    @Column(columnDefinition = "TEXT")
    private String treatmentRecommendation;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Report(Match match, java.time.LocalDate startDate, java.time.LocalDate endDate, String content,
                  Integer totalScore, String riskLevel, String phq9Slots,
                  String treatmentRecommendation) {
        this.match = match;
        this.startDate = startDate;
        this.endDate = endDate;
        this.content = content;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.phq9Slots = phq9Slots;
        this.treatmentRecommendation = treatmentRecommendation;
    }
}
