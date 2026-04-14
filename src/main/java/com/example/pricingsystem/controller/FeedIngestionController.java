package com.example.pricingsystem.controller;

import com.example.pricingsystem.service.FeedIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeedIngestionController {

    private final FeedIngestionService ingestionService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadPricingFeed(@RequestParam("file") MultipartFile file) {

        String trackingId = ingestionService.acceptFile(file);

        Map<String, String> response = new HashMap<>();
        response.put("message", "File uploaded successfully and is queued for processing.");
        response.put("trackingId", trackingId);

        // Return 202 Accepted because the processing happens asynchronously
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}