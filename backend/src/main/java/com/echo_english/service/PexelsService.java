package com.echo_english.service;

import com.echo_english.entity.PexelsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PexelsService {

    private static final Logger logger = LoggerFactory.getLogger(PexelsService.class);

    @Autowired
    private RestTemplate restTemplate; // Inject RestTemplate (cần tạo Bean cho nó)

    @Value("${pexels.api.key}") // Inject key từ application.properties
    private String pexelsApiKey;

    @Value("${pexels.api.baseurl}") // Inject base URL từ application.properties
    private String pexelsBaseUrl;

    public PexelsResponse searchPexelsImages(String query, int perPage, int page, String orientation) {
        if (pexelsApiKey == null || pexelsApiKey.isEmpty() || pexelsApiKey.equals("YOUR_PEXELS_API_KEY_HERE")) {
            logger.error("Pexels API Key is not configured properly!");
            // Có thể ném Exception hoặc trả về null/rỗng
            return null;
        }

        // Tạo Headers với API Key
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", pexelsApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Tạo URL với Query Parameters
        String url = UriComponentsBuilder.fromHttpUrl(pexelsBaseUrl + "/search")
                .queryParam("query", query)
                .queryParam("per_page", perPage)
                .queryParam("page", page)
                .queryParam("orientation", orientation)
                .encode() // Mã hóa các tham số
                .toUriString();

        logger.info("Calling Pexels API: {}", url);

        try {
            // Gọi API bằng RestTemplate
            ResponseEntity<PexelsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PexelsResponse.class // Kiểu dữ liệu mong đợi trả về
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Pexels API call successful. Found {} results.",
                        response.getBody() != null ? response.getBody().getTotalResults() : 0);
                return response.getBody();
            } else {
                logger.error("Pexels API call failed with status: {} - {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            logger.error("Pexels API client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Error calling Pexels API", e);
            return null;
        }
    }
}