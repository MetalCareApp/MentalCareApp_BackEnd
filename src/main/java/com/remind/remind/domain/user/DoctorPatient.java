package com.remind.remind.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_patients")
public class DoctorPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User patient; // 환자(일반 유저)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MappingStatus status; // PENDING, ACCEPTED, REJECTED

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DoctorPatient(Doctor doctor, User patient, MappingStatus status) {
        this.doctor = doctor;
        this.patient = patient;
        this.status = status;
    }

    public void updateStatus(MappingStatus status) {
        this.status = status;
    }
}
