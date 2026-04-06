package com.remind.remind.service.user;

import com.remind.remind.domain.user.Doctor;
import com.remind.remind.repository.user.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }
}
