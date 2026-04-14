package com.example.pricingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingRecordDto {
    private Long id;
    private String storeId;
    private String sku;
    private String productName;
    private BigDecimal price;
    private String currency;
    private LocalDate effectiveDate;

    // We send the version to the frontend. When the frontend sends an edit request,
    // it must include this version so the backend can verify no one else changed it.
    private Long version;
}