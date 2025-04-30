package com.echo_english.controller;

import com.echo_english.entity.PexelsResponse;
import com.echo_english.service.PexelsService; // Import PexelsService
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pixels") // Base path cho các API liên quan đến ảnh
@RequiredArgsConstructor
public class PixelsController {

    private final PexelsService pexelsService;

    // Endpoint mới để tìm ảnh qua Pexels
    @GetMapping("/search")
    public ResponseEntity<PexelsResponse> searchImages(
            @RequestParam String query, // Nhận từ khóa từ client
            @RequestParam(defaultValue = "12") int perPage, // Giá trị mặc định
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "landscape") String orientation) {

        PexelsResponse response = pexelsService.searchPexelsImages(query, perPage, page, orientation);

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            // Có thể trả về lỗi cụ thể hơn dựa trên lỗi từ PexelsService
            return ResponseEntity.status(503).body(null); // 503 Service Unavailable (ví dụ)
        }
    }
}