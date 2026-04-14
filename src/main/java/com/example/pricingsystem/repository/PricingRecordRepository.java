package com.example.pricingsystem.repository;

import com.example.pricingsystem.entity.PricingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PricingRecordRepository extends JpaRepository<PricingRecord, Long> {

    // Used by the SPA to search for prices. Enforces Pagination.
    Page<PricingRecord> findByStoreIdAndSkuContainingIgnoreCase(String storeId, String sku, Pageable pageable);

    // Used by the SPA to view all prices for a store. Enforces Pagination.
    Page<PricingRecord> findByStoreId(String storeId, Pageable pageable);

    // Used by the Spring Batch worker to check if a record already exists
    // so it can perform an "Upsert" (Update if exists, Insert if new).
    Optional<PricingRecord> findByStoreIdAndSkuAndEffectiveDate(String storeId, String sku, LocalDate effectiveDate);
}