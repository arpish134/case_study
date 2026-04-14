package com.example.pricingsystem.service;

import com.example.pricingsystem.dto.PricingRecordDto;
import com.example.pricingsystem.entity.PricingRecord;
import com.example.pricingsystem.repository.PricingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRecordRepository repository;

    // 1. Search by Store ID and SKU (Enforces Pagination)
    public Page<PricingRecordDto> searchPrices(String storeId, String sku, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("effectiveDate").descending());

        Page<PricingRecord> records = repository.findByStoreIdAndSkuContainingIgnoreCase(storeId, sku, pageable);
        return records.map(this::convertToDto);
    }

    // 2. Get all prices for a store
    public Page<PricingRecordDto> getPricesForStore(String storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("effectiveDate").descending());

        Page<PricingRecord> records = repository.findByStoreId(storeId, pageable);
        return records.map(this::convertToDto);
    }

    // 3. Manual Edit by Admin (Protected by Optimistic Locking)
    @Transactional
    public PricingRecordDto updatePrice(Long id, PricingRecordDto updateDto) {
        PricingRecord existingRecord = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing record not found for ID: " + id));

        // The @Version annotation on the entity handles the concurrent modification check automatically.
        // If the version in the DB doesn't match the version the entity currently has in memory,
        // Hibernate throws an ObjectOptimisticLockingFailureException.

        existingRecord.setPrice(updateDto.getPrice());
        // In a real app, you might allow updating effective dates or names too, based on business rules

        PricingRecord savedRecord = repository.save(existingRecord);
        return convertToDto(savedRecord);
    }

    // Helper method to map Entity -> DTO (In a real app, use MapStruct for this)
    private PricingRecordDto convertToDto(PricingRecord record) {
        PricingRecordDto dto = new PricingRecordDto();
        dto.setId(record.getId());
        dto.setStoreId(record.getStoreId());
        dto.setSku(record.getSku());
        dto.setProductName(record.getProductName());
        dto.setPrice(record.getPrice());
        dto.setCurrency(record.getCurrency());
        dto.setEffectiveDate(record.getEffectiveDate());
        dto.setVersion(record.getVersion()); // Critical: send version to frontend
        return dto;
    }
}