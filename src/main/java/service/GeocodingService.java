package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeocodingService {

    @Value("${kakao.map.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final String KAKAO_MAP_API_BASE_URL = "https://dapi.kakao.com/v2/local";

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    public CoordinateResult addressToCoordinate(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_MAP_API_BASE_URL + "/search/address.json")
                .queryParam("query", address)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");

            if (documents != null && !documents.isEmpty()) {
                Map<String, Object> firstResult = documents.get(0);
                double x = Double.parseDouble(firstResult.get("x").toString());
                double y = Double.parseDouble(firstResult.get("y").toString());
                String roadAddress = (String) firstResult.get("road_address");
                String jibunAddress = (String) firstResult.get("address");

                log.info("주소 -> 좌표 변환 성공: {} -> ({}, {})", address, x, y);
                return new CoordinateResult(x, y, roadAddress, jibunAddress, true);
            } else {
                log.warn("주소를 찾을 수 없음: {}", address);
                return new CoordinateResult(0, 0, null, null, false);
            }
        } catch (Exception e) {
            log.error("주소 -> 좌표 변환 실패: {}", e.getMessage());
            return new CoordinateResult(0, 0, null, null, false);
        }
    }

    public AddressResult coordinateToAddress(double x, double y) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_MAP_API_BASE_URL + "/geo/coord2address.json")
                .queryParam("x", x)
                .queryParam("y", y)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");

            if (documents != null && !documents.isEmpty()) {
                Map<String, Object> firstResult = documents.get(0);
                Map<String, Object> address = (Map<String, Object>) firstResult.get("address");
                Map<String, Object> roadAddress = (Map<String, Object>) firstResult.get("road_address");

                String jibunAddress = buildJibunAddress(address);
                String roadAddressName = roadAddress != null ? (String) roadAddress.get("address_name") : null;

                log.info("좌표 -> 주소 변환 성공: ({}, {}) -> {}", x, y, jibunAddress);
                return new AddressResult(jibunAddress, roadAddressName, true);
            } else {
                log.warn("좌표에 해당하는 주소를 찾을 수 없음: ({}, {})", x, y);
                return new AddressResult(null, null, false);
            }
        } catch (Exception e) {
            log.error("좌표 -> 주소 변환 실패: {}", e.getMessage());
            return new AddressResult(null, null, false);
        }
    }

    private String buildJibunAddress(Map<String, Object> address) {
        if (address == null) return null;

        StringBuilder sb = new StringBuilder();

        String region1 = (String) address.get("region_1depth_name");
        String region2 = (String) address.get("region_2depth_name");
        String region3 = (String) address.get("region_3depth_name");
        String mainNo = (String) address.get("main_address_no");
        String subNo = (String) address.get("sub_address_no");

        if (region1 != null) sb.append(region1).append(" ");
        if (region2 != null) sb.append(region2).append(" ");
        if (region3 != null) sb.append(region3).append(" ");
        if (mainNo != null) sb.append(mainNo);
        if (subNo != null && !subNo.isEmpty() && !subNo.equals("0")) {
            sb.append("-").append(subNo);
        }

        return sb.toString().trim();
    }

    public static class CoordinateResult {
        public final double longitude;
        public final double latitude;
        public final String roadAddress;
        public final String jibunAddress;
        public final boolean success;

        public CoordinateResult(double longitude, double latitude, String roadAddress, String jibunAddress, boolean success) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.roadAddress = roadAddress;
            this.jibunAddress = jibunAddress;
            this.success = success;
        }
    }

    public static class AddressResult {
        public final String jibunAddress;
        public final String roadAddress;
        public final boolean success;

        public AddressResult(String jibunAddress, String roadAddress, boolean success) {
            this.jibunAddress = jibunAddress;
            this.roadAddress = roadAddress;
            this.success = success;
        }
    }
}