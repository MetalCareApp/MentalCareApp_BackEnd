package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.domain.user.MappingStatus;
import com.remind.remind.dto.user.MappingRequest;
import com.remind.remind.dto.user.MappingResponse;
import com.remind.remind.service.user.DoctorCommandService;
import com.remind.remind.service.user.DoctorQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {

    private final DoctorCommandService doctorCommandService;
    private final DoctorQueryService doctorQueryService;

    /**
     * 매칭 요청 전송 (의사 -> 환자)
     */
    @PostMapping
    public ResponseEntity<MappingResponse> requestMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody MappingRequest request) {
        
        MappingResponse response = doctorCommandService.requestMatching(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 나에게 온 매칭 요청 목록 조회 (주로 환자용, 알림 근거 데이터)
     */
    @GetMapping("/requests")
    public ResponseEntity<List<MappingResponse>> getReceivedRequests(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<MappingResponse> responses = doctorQueryService.getPatientRequests(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭 요청 수락
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<MappingResponse> acceptMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long mappingId) {
        
        MappingResponse response = doctorCommandService.updateMappingStatus(mappingId, MappingStatus.ACCEPTED, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 요청 거절
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<MappingResponse> rejectMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long mappingId) {
        
        MappingResponse response = doctorCommandService.updateMappingStatus(mappingId, MappingStatus.REJECTED, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 활성화된 매칭 목록 조회 (의사: 관리 환자들 / 환자: 담당 의사들)
     */
    @GetMapping
    public ResponseEntity<List<MappingResponse>> getActiveMatches(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        if (principalDetails.getUser().isDoctor()) {
            List<MappingResponse> responses = doctorQueryService.getAcceptedPatients(principalDetails.getUser().getId());
            return ResponseEntity.ok(responses);
        } else {
            List<MappingResponse> responses = doctorQueryService.getAcceptedDoctors(principalDetails.getUser().getId());
            return ResponseEntity.ok(responses);
        }
    }

    /**
     * 매칭 해제 (연결 종료)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long mappingId) {
        
        doctorCommandService.deleteMapping(mappingId, principalDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
