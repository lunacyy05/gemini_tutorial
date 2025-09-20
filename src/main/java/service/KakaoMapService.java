package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
public class KakaoMapService {

    @Value("${kakao.map.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final String KAKAO_MAP_API_BASE_URL = "https://dapi.kakao.com/v2/local";

    public KakaoMapService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> searchByKeyword(String keyword, int page, int size) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_MAP_API_BASE_URL + "/search/keyword.json")
                .queryParam("query", keyword)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            log.info("카카오 맵 키워드 검색 성공: {}", keyword);
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 맵 키워드 검색 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 맵 API 호출 실패", e);
        }
    }

    public Map<String, Object> searchByCategory(String categoryGroupCode, double x, double y, int radius, int page, int size) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_MAP_API_BASE_URL + "/search/category.json")
                .queryParam("category_group_code", categoryGroupCode)
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("radius", radius)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            log.info("카카오 맵 카테고리 검색 성공: {} at ({}, {})", categoryGroupCode, x, y);
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 맵 카테고리 검색 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 맵 API 호출 실패", e);
        }
    }

    public Map<String, Object> getNearbyProperties(double x, double y, int radius) {
        return searchByCategory("MT1", x, y, radius, 1, 15);
    }

    public boolean isValidApiKey() {
        try {
            searchByKeyword("테스트", 1, 1);
            return true;
        } catch (Exception e) {
            log.warn("카카오 맵 API 키 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}