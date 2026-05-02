package com.remind.remind.service.report;

import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;

    public List<ReportResponse> getReports(Long userId) {
        return reportRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    public ReportResponse getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .map(ReportResponse::from)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR)); // TODO: REPORT_NOT_FOUND 추가 필요
    }
}
