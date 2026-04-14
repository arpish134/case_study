package com.example.pricingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pricing_records",
        // Prevent duplicate entries for the same product in the same store on the same day
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"store_id", "sku", "effective_date"})
        },
        // Indexes to ensure sub-second search performance
        indexes = {
                @Index(name = "idx_store_sku", columnList = "store_id, sku"),
                @Index(name = "idx_effective_date", columnList = "effective_date")
        }
)
@Data // Lombok: auto-generates getters, setters, toString, etc.
@NoArgsConstructor
public class PricingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "product_name", nullable = false)
    private String productName;

    // Always use BigDecimal for currency to prevent precision loss
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    // --- DESIGN DECISION: Optimistic Locking ---
    // If a CSV feed and an Admin try to update this exact record at the same time,
    // the database will reject the second attempt rather than silently overwriting data.
    @Version
    private Long version;

    // --- Auditing Fields ---
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Automatically set timestamps before saving to the DB
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}