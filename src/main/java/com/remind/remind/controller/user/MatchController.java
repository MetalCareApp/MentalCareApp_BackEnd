package com.remind.remind.controller.user;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.dto.user.MatchRequest;
import com.remind.remind.dto.user.MatchResponse;
import com.remind.remind.service.user.DoctorCommandService;
import com.remind.remind.service.user.DoctorQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.remind.remind.dto.user.MatchDetailResponse;

import com.remind.remind.dto.user.PatientSearchResponse;

import com.remind.remind.dto.user.MatchedPatientResponse;

import com.remind.remind.dto.user.MatchedPatientDetailResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {

    private final DoctorCommandService doctorCommandService;
    private final DoctorQueryService doctorQueryService;

    /**
     * 의사용 환자 상세 정보 조회 (환자 프로필 + AI 리포트 목록)
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<MatchedPatientDetailResponse> getMatchedPatientDetail(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long matchId) {
        
        MatchedPatientDetailResponse response = doctorQueryService.getMatchedPatientDetail(matchId, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 활성화된 매칭 목록 조회 (의사 전용: 관리 환자들)
     * 상태가 ACCEPTED인 환자들만 반환합니다.
     */
    @GetMapping
    public ResponseEntity<List<MatchedPatientResponse>> getAcceptedPatients(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<MatchedPatientResponse> responses = doctorQueryService.getAcceptedPatients(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 환자 검색 (의사용)
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientSearchResponse>> searchPatients(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("email") String email) {
        
        List<PatientSearchResponse> responses = doctorQueryService.searchPatients(email, principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭 상세 정보 조회 (환자용, 마이페이지 병원 상세 정보)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MatchDetailResponse> getMatchDetail(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long matchId) {
        
        MatchDetailResponse response = doctorQueryService.getMatchDetail(matchId, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 요청 전송 (의사 -> 환자)
     */
    @PostMapping
    public ResponseEntity<MatchResponse> requestMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody MatchRequest request) {
        
        MatchResponse response = doctorCommandService.requestMatching(principalDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
    /**
     * 나에게 온 매칭 요청 목록 조회 (주로 환자용, 알림 근거 데이터)
     */
    /*
    @GetMapping("/requests")
    public ResponseEntity<List<MatchResponse>> getReceivedRequests(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<MatchResponse> responses = doctorQueryService.getPatientRequests(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }
    */

    /**
     * 매칭 요청 수락
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<MatchResponse> acceptMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long matchId) {
        
        MatchResponse response = doctorCommandService.updateMatchStatus(matchId, MatchStatus.ACCEPTED, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 요청 거절
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<MatchResponse> rejectMatching(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long matchId) {
        
        MatchResponse response = doctorCommandService.updateMatchStatus(matchId, MatchStatus.REJECTED, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 나에게 온 매칭 요청 목록 조회 (환자용, 알림 페이지 데이터)
     * 상태가 PENDING인 요청만 반환합니다.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<MatchResponse>> getMatchRequests(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        List<MatchResponse> responses = doctorQueryService.getPatientRequests(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭 해제 (연결 종료)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("id") Long matchId) {
        
        doctorCommandService.deleteMatch(matchId, principalDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
