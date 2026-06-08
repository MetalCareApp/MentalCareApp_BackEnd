package com.remind.remind.service.report;

import com.remind.remind.dto.report.ReportResponse;
import com.remind.remind.exception.BaseException;
import com.remind.remind.exception.ErrorCode;
import com.remind.remind.repository.report.ReportRepository;
import com.remind.remind.domain.diary.Diary;
import com.remind.remind.repository.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.remind.remind.domain.user.Match;
import com.remind.remind.domain.user.MatchStatus;
import com.remind.remind.repository.user.MatchRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;
    private final MatchRepository matchRepository;
    private final DiaryRepository diaryRepository;

    public List<ReportResponse> getReports(Long userId) {
        // 해당 유저의 수락된 매칭 확인 (여러 개일 수 있으므로 모두의 리포트 조회)
        List<Match> matches = matchRepository.findAllByPatientIdAndStatus(userId, MatchStatus.ACCEPTED);
        
        List<ReportResponse> responses = new ArrayList<>();
        for (Match match : matches) {
            responses.addAll(reportRepository.findAllByMatchIdOrderByCreatedAtDesc(match.getId()).stream()
                    .map(report -> {
                        List<Diary> diaries = diaryRepository.findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(
                                match.getPatient(), report.getStartDate(), report.getEndDate());
                        return ReportResponse.from(report, diaries);
                    })
                    .collect(Collectors.toList()));
        }
        return responses;
    }

    public ReportResponse getReport(Long reportId) {
        com.remind.remind.domain.report.Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));
        
        List<Diary> diaries = diaryRepository.findAllByUserAndDiaryDateBetweenOrderByDiaryDateAsc(
                report.getMatch().getPatient(), report.getStartDate(), report.getEndDate());
                
        return ReportResponse.from(report, diaries);
    }
}
