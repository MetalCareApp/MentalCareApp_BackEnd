package com.remind.remind.domain.user;

import com.remind.remind.domain.hospital.Hospital;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctors")
@SQLDelete(sql = "UPDATE doctors SET deleted_at = CURRENT_TIMESTAMP WHERE doctor_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "patient_count")
    private Integer patientCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status; // PENDING, ACCEPTED, REJECTED

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Match> patients = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Doctor(User user, Hospital hospital, Integer patientCount, MatchStatus status) {
        this.user = user;
        this.hospital = hospital;
        this.patientCount = patientCount;
        this.status = status;
    }

    public void updateStatus(MatchStatus status) {
        this.status = status;
    }
}
