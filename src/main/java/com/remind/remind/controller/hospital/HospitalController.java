package com.remind.remind.controller.hospital;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.hospital.HospitalResponse;
import com.remind.remind.service.hospital.HospitalCommandService;
import com.remind.remind.service.hospital.HospitalQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hospitals")
public class HospitalController {

    private final HospitalQueryService hospitalQueryService;
    private final HospitalCommandService hospitalCommandService;

    /**
     * 병원 전체 목록 조회 (검색 및 지역 필터 포함)
     */
    @GetMapping
    public ResponseEntity<List<HospitalResponse>> getAllHospitals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String region,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<HospitalResponse> response = hospitalQueryService.getAllHospitals(
                principalDetails.getUser().getId(), name, region);
        return ResponseEntity.ok(response);
    }

    /**
     * 병원 상세 정보 조회
     */
    @GetMapping("/{hospitalId}")
    public ResponseEntity<HospitalResponse> getHospitalDetail(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        HospitalResponse response = hospitalQueryService.getHospitalDetail(hospitalId, principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 병원 찜하기
     */
    @PostMapping("/{hospitalId}/likes")
    public ResponseEntity<Void> likeHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        hospitalCommandService.likeHospital(principalDetails.getUser().getId(), hospitalId);
        return ResponseEntity.ok().build();
    }

    /**
     * 찜한 병원 목록 조회
     */
    @GetMapping("/likes")
    public ResponseEntity<List<HospitalResponse>> getLikedHospitals(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<HospitalResponse> response = hospitalQueryService.getLikedHospitals(principalDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 병원 찜 취소
     */
    @DeleteMapping("/likes/{hospitalId}")
    public ResponseEntity<Void> unlikeHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        hospitalCommandService.unlikeHospital(principalDetails.getUser().getId(), hospitalId);
        return ResponseEntity.noContent().build();
    }
}
