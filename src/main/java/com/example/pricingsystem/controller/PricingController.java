package com.example.pricingsystem.controller;

import com.example.pricingsystem.dto.PricingRecordDto;
import com.example.pricingsystem.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend to call these APIs (Configure properly in production!)
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<PricingRecordDto>> getStorePrices(
            @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(pricingService.getPricesForStore(storeId, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PricingRecordDto>> searchPrices(
            @RequestParam String storeId,
            @RequestParam String sku,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(pricingService.searchPrices(storeId, sku, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingRecordDto> updatePrice(
            @PathVariable Long id,
            @RequestBody PricingRecordDto updateDto) {

        return ResponseEntity.ok(pricingService.updatePrice(id, updateDto));
    }
}