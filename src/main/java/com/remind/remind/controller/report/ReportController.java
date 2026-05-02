package com.remind.remind.controller.report;

import com.remind.remind.config.security.PrincipalDetails;
import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.service.report.ReportCommandService;
import com.remind.remind.service.report.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;

    /**
     * AI 리포트 생성
     */
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        ReportResponse response = reportCommandService.createAiReport(principalDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 리포트 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<ReportResponse> responses = reportQueryService.getReports(principalDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 리포트 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        ReportResponse response = reportQueryService.getReport(id);
        return ResponseEntity.ok(response);
    }
}
