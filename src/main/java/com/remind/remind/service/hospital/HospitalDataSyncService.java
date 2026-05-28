package com.remind.remind.service.hospital;

import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.repository.hospital.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalDataSyncService {

    private final HospitalRepository hospitalRepository;
    private final HospitalSyncProcessor hospitalSyncProcessor;

    /**
     * [자바 21 가상 스레드 모드 + 세마포어 조절]
     * 동기화 작업 완료 후 일괄 클린업을 수행합니다.
     */
    @Async
    public void syncDoctorCounts(boolean force) {
        List<Hospital> hospitals;
        if (force) {
            log.info(">>> FORCE SYNC REQUESTED. Fetching all hospitals...");
            hospitals = hospitalRepository.findAll();
        } else {
            // [개선] 
            // 1. 개점일이 없는 병원 (필수 정보 미수집)은 무조건 포함
            // 2. 개점일이 있더라도 업데이트된 지 1시간이 지난 데이터는 포함 (어제 트래픽 소진 시점 데이터 구제)
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);
            hospitals = hospitalRepository.findAllToSync(threshold);
        }
        
        int total = hospitals.size();
        if (total == 0) {
            log.info(">>> All hospitals are up to date. No sync needed.");
            return;
        }

        log.info(">>> TARGET SEARCH: Found {} hospitals needing sync (Null data or stale).", total);
        log.info(">>> VIRTUAL THREAD SYNC STARTED: {} hospitals targets.", total);

        AtomicInteger processedCount = new AtomicInteger(0);
        Semaphore semaphore = new Semaphore(40); 

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Hospital hospital : hospitals) {
                Long hospitalId = hospital.getId();
                executor.submit(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            hospitalSyncProcessor.process(hospital);
                        } finally {
                            semaphore.release();
                        }
                        
                        int current = processedCount.incrementAndGet();
                        if (current % 50 == 0 || current == total) {
                            log.info("Progress: {}/{} ({}%)", current, total, (current * 100 / total));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error("Failed to process hospital ID {}: {}", hospitalId, e.getMessage());
                    }
                });
            }
            
            executor.shutdown();
            if (!executor.awaitTermination(120, TimeUnit.MINUTES)) {
                log.warn("Sync timed out after 120 minutes.");
            }

            // [안전장치] 모든 동기화 작업이 끝난 후, 개점일은 있는데 의사가 0명인 병원 일괄 삭제
            hospitalSyncProcessor.performBatchCleanup();

        } catch (InterruptedException e) {
            log.error("Sync interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info(">>> VIRTUAL THREAD SYNC COMPLETED. Total processed: {}", processedCount.get());
    }
}
