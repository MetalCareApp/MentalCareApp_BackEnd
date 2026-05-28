package com.remind.remind.controller.hospital;

import com.remind.remind.service.hospital.HospitalDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hospitals/admin")
public class HospitalAdminController {

    private final HospitalDataSyncService hospitalDataSyncService;

    /**
     * 공공데이터 API를 통해 병원의 전문의/일반의 수를 동기화합니다.
     * @param force true일 경우 24시간 제한을 무시하고 전체 병원을 대상으로 수행합니다.
     */
    @PostMapping("/sync-doctors")
    public ResponseEntity<String> syncDoctorCounts(@org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "false") boolean force) {
        hospitalDataSyncService.syncDoctorCounts(force);
        return ResponseEntity.ok("Doctor count synchronization started. (Force: " + force + "). Check logs for details.");
    }
}
