package com.remind.remind.service.hospital;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remind.remind.domain.hospital.Hospital;
import com.remind.remind.repository.hospital.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HospitalSyncProcessor {

    private final HospitalRepository hospitalRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${public.data.service-key}")
    private String serviceKey;

    // [Enrichment] 상세 정보 (개점일, 의사 수) - 800,000 한도 서비스
    private static final String DETAIL_BASE_URL = "https://apis.data.go.kr/B551182/MadmDtlInfoService2.7";
    
    // [Discovery] 고유번호(ykiho) 탐색용 서비스
    private static final String BASIS_URL_V2 = "https://apis.data.go.kr/B551182/hospInfoServicev2/getHospBasisList";
    private static final String BASIS_URL_V1 = "https://apis.data.go.kr/B551182/hospInfoService/getHospBasisList";

    private static final List<String> NOISE_KEYWORDS = Arrays.asList(
            "이비인후과", "치과", "한방", "성형외과", "피부과", "안과", "비뇨", "산부인과", "소아과", "외과", "내과", "요양", "정형"
    );

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Hospital hospital) {
        if (hospital == null) return;

        boolean trace = hospital.getName().contains("서울대학교병원");
        if (trace) log.info(">>> [TRACER] Starting focused sync for: {} (ID: {})", hospital.getName(), hospital.getApiId());

        if (isNoiseHospital(hospital.getName())) {
            markAsDeleted(hospital);
            return;
        }

        try {
            String realYkiho = hospital.getApiId();
            
            // 1. 진짜 고유번호(ykiho) 탐색 (v2 우선, v1 fallback)
            if (realYkiho == null || realYkiho.isEmpty() || realYkiho.contains("recover_")) {
                realYkiho = discoverRealYkiho(hospital, trace);
            }

            if (realYkiho == null || realYkiho.isEmpty()) {
                if (trace) log.warn(">>> [TRACER] Failed to find ykiho for {}", hospital.getName());
                return;
            }

            // 2. 개점일 수집
            LocalDate openingDate = hospital.getOpeningDate();
            if (openingDate == null) {
                openingDate = fetchOpeningDate(realYkiho, trace);
                // ykiho가 잘못되었을 가능성 대비 재탐색
                if (openingDate == null) {
                    if (trace) log.info(">>> [TRACER] No detail found for ykiho {}, trying rediscovery...", realYkiho);
                    String newYkiho = discoverRealYkiho(hospital, trace);
                    if (newYkiho != null && !newYkiho.equals(realYkiho)) {
                        realYkiho = newYkiho;
                        openingDate = fetchOpeningDate(realYkiho, trace);
                    }
                }
            }

            // 3. 정신과 의사 데이터 수집
            PsychiatryData doctorData = fetchPsychiatryData(hospital.getName(), realYkiho, trace);

            // 4. 결과 저장
            saveResults(hospital.getId(), realYkiho, openingDate, doctorData, trace);

        } catch (Exception e) {
            log.error(">>> [ERROR] {}: {}", hospital.getName(), e.getMessage());
        }
    }

    public void saveResults(Long hospitalId, String realYkiho, LocalDate openingDate, PsychiatryData data, boolean trace) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
        if (hospital == null) return;

        try {
            // [중요] 중복 체크: 이미 해당 api_id를 가진 다른 병원(소프트 딜리트 포함)이 있는지 확인
            if (realYkiho != null) {
                java.util.Optional<Hospital> existing = hospitalRepository.findByApiIdIncludeDeleted(realYkiho);
                if (existing.isPresent() && !existing.get().getId().equals(hospitalId)) {
                    log.warn(">>> [DUPLICATE] Hospital '{}' (ID: {}) has same api_id as ID: {}. Removing redundant record.", 
                             hospital.getName(), hospitalId, existing.get().getId());
                    hospitalRepository.delete(hospital);
                    return;
                }
            }

            boolean isChanged = false;
            if (realYkiho != null && !realYkiho.equals(hospital.getApiId())) {
                hospital.updateApiId(realYkiho);
                isChanged = true;
            }
            if (openingDate != null && !openingDate.equals(hospital.getOpeningDate())) {
                hospital.updateOpeningDate(openingDate);
                isChanged = true;
            }
            if (data != null) {
                hospital.updateDoctorCounts(data.specialistCount, data.generalDoctorCount);
                if (data.hasPsychiatry) {
                    log.info(">>> [SUCCESS] {} (Spec: {})", hospital.getName(), data.specialistCount);
                } else {
                    log.info(">>> [SYNCED] {} - No psychiatry staff", hospital.getName());
                }
                isChanged = true;
            }

            if (isChanged) {
                hospitalRepository.saveAndFlush(hospital);
                if (trace) log.info(">>> [TRACER] Saved changes for {}", hospital.getName());
            } else {
                if (trace) log.info(">>> [TRACER] No changes for {}", hospital.getName());
            }
        } catch (Exception e) {
            log.error(">>> [SAVE ERROR] {}: {}", hospital.getName(), e.getMessage());
        }
    }

    private String discoverRealYkiho(Hospital hospital, boolean trace) {
        String name = hospital.getName();
        String province = hospital.getProvince();

        // 1단계: 정확한 이름 검색 (V2 -> V1)
        String ykiho = callBasisApi(BASIS_URL_V2, name, province, trace);
        if (ykiho == null) ykiho = callBasisApi(BASIS_URL_V1, name, province, trace);
        if (ykiho != null) return ykiho;

        // 2단계: 지역명 + 이름 검색 (V2 -> V1)
        if (province != null && province.length() >= 2) {
            String combined = province.substring(0, 2) + " " + name;
            ykiho = callBasisApi(BASIS_URL_V2, combined, province, trace);
            if (ykiho == null) ykiho = callBasisApi(BASIS_URL_V1, combined, province, trace);
            if (ykiho != null) return ykiho;
        }

        return null;
    }

    private String callBasisApi(String baseUrl, String name, String province, boolean trace) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("yadmNm", name)
                    .queryParam("_type", "json")
                    .build()
                    .encode()
                    .toUri();

            String response = callApi(uri, trace);
            if (response == null || response.contains("LIMITED_NUMBER_OF_SERVICE_REQUEST_EXCEEDS_ERROR")) return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");
            String provinceKey = (province != null && province.length() >= 2) ? province.substring(0, 2) : "";

            if (items.isArray()) {
                for (JsonNode item : items) {
                    if (provinceKey.isEmpty() || item.path("addr").asText("").contains(provinceKey)) 
                        return item.path("ykiho").asText();
                }
            } else if (!items.isMissingNode()) {
                if (provinceKey.isEmpty() || items.path("addr").asText("").contains(provinceKey)) 
                    return items.path("ykiho").asText();
            }
        } catch (Exception e) { return null; }
        return null;
    }

    private LocalDate fetchOpeningDate(String ykiho, boolean trace) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(DETAIL_BASE_URL + "/getEqpInfo2.7")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("ykiho", ykiho)
                    .queryParam("_type", "json")
                    .build()
                    .encode()
                    .toUri();
            
            String response = callApi(uri, trace);
            if (trace) log.info(">>> [RAW DETAIL RESPONSE] For ykiho {}: {}", ykiho, response);

            if (response == null || response.contains("LIMITED_NUMBER_OF_SERVICE_REQUEST_EXCEEDS_ERROR")) return null;

            JsonNode root = objectMapper.readTree(response);
            int totalCount = root.path("response").path("body").path("totalCount").asInt(0);
            if (totalCount == 0) return null;

            JsonNode item = root.path("response").path("body").path("items").path("item");
            if (item.isArray()) item = item.get(0);

            String estbDd = item.path("estbDd").asText("");
            if (estbDd.length() == 8) {
                return LocalDate.of(Integer.parseInt(estbDd.substring(0, 4)), 
                                    Integer.parseInt(estbDd.substring(4, 6)), 
                                    Integer.parseInt(estbDd.substring(6, 8)));
            }
        } catch (Exception e) { return null; }
        return null;
    }

    private PsychiatryData fetchPsychiatryData(String name, String ykiho, boolean trace) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(DETAIL_BASE_URL + "/getSpcSbjtSdrInfo2.7")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("ykiho", ykiho)
                    .queryParam("_type", "json")
                    .build()
                    .encode()
                    .toUri();
            
            String response = callApi(uri, trace);
            if (trace) log.info(">>> [RAW DOCTOR RESPONSE] For ykiho {}: {}", ykiho, response);

            if (response == null || response.contains("LIMITED_NUMBER_OF_SERVICE_REQUEST_EXCEEDS_ERROR")) return null;

            JsonNode root = objectMapper.readTree(response);
            int totalCount = root.path("response").path("body").path("totalCount").asInt(0);
            
            // 만약 ykiho 자체가 없으면 null 반환 (0명으로 오해하지 않도록)
            if (totalCount == 0) return null;

            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    if ("03".equals(item.path("dgsbjtCd").asText(""))) 
                        return new PsychiatryData(true, item.path("dtlSdrCnt").asInt(0), 0);
                }
            } else if (!items.isMissingNode()) {
                if ("03".equals(items.path("dgsbjtCd").asText(""))) 
                    return new PsychiatryData(true, items.path("dtlSdrCnt").asInt(0), 0);
            }
            
            // 병원은 존재하는데 정신과(03) 정보가 없는 경우 -> 정신과 의사 0명으로 확정
            return new PsychiatryData(false, 0, 0);
        } catch (Exception e) { return null; }
    }

    private String callApi(URI uri, boolean trace) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return response.getBody();
        } catch (Exception e) {
            if (trace) log.error(">>> [API ERROR] URL: {}, Error: {}", uri, e.getMessage());
            return null;
        }
    }

    private boolean isNoiseHospital(String name) {
        if (name.contains("종합") || name.contains("대학") || name.contains("의료원") || name.contains("센터") || name.contains("국립")) return false;
        if (name.contains("정신건강")) return false;
        for (String k : NOISE_KEYWORDS) if (name.contains(k)) return true;
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsDeleted(Hospital h) { hospitalRepository.delete(h); }

    @Transactional
    public void performBatchCleanup() {
        log.info(">>> STARTING BATCH CLEANUP...");
        int deletedCount = hospitalRepository.softDeleteNoPsychiatryHospitals();
        log.info(">>> BATCH CLEANUP COMPLETED. Removed {} invalid hospitals.", deletedCount);
    }

    private static class PsychiatryData {
        boolean hasPsychiatry; int specialistCount; int generalDoctorCount;
        PsychiatryData(boolean h, int s, int g) { this.hasPsychiatry = h; this.specialistCount = s; this.generalDoctorCount = g; }
    }
}
