package com.example.pricingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CsvPricingRecord {
    private String storeId;
    private String sku;
    private String productName;
    private BigDecimal price;
    private String effectiveDate; // Read as String first, parsed later
}