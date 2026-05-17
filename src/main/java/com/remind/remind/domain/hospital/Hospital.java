package com.remind.remind.domain.hospital;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "hospitals")
@SQLDelete(sql = "UPDATE hospitals SET deleted_at = CURRENT_TIMESTAMP WHERE hospital_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "api_id", nullable = false, unique = true, length = 100)
    private String apiId;

    @Column(nullable = false)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "specialist_count")
    private Integer specialistCount;

    @Column(name = "general_doctor_count")
    private Integer generalDoctorCount;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Hospital(String name, String apiId, String address, String phone, LocalDate openingDate, Integer specialistCount, Integer generalDoctorCount) {
        this.name = name;
        this.apiId = apiId;
        this.address = address;
        this.phone = phone;
        this.openingDate = openingDate;
        this.specialistCount = specialistCount;
        this.generalDoctorCount = generalDoctorCount;
    }
}
