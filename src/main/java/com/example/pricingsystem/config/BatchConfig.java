package com.example.pricingsystem.config;

import com.example.pricingsystem.dto.CsvPricingRecord;
import com.example.pricingsystem.entity.PricingRecord;
import com.example.pricingsystem.repository.PricingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final PricingRecordRepository repository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // 1. READER: Reads the CSV file line by line
    @Bean
    @StepScope // Instantiated dynamically when the job runs with the specific file path
    public FlatFileItemReader<CsvPricingRecord> reader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<CsvPricingRecord>()
                .name("csvItemReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1) // Skip CSV Header
                .delimited()
                .names("storeId", "sku", "productName", "price", "effectiveDate")
                .targetType(CsvPricingRecord.class)
                .build();
    }

    // 2. PROCESSOR: Validates and converts CSV Row -> DB Entity
    @Bean
    public ItemProcessor<CsvPricingRecord, PricingRecord> processor() {
        return csvRecord -> {
            // Validation example
            if (csvRecord.getPrice().signum() < 0) {
                return null; // Skip records with negative prices
            }

            LocalDate parsedDate = LocalDate.parse(csvRecord.getEffectiveDate(), DateTimeFormatter.ISO_DATE);

            // Check if record already exists (Upsert Logic)
            Optional<PricingRecord> existingRecordOpt = repository.findByStoreIdAndSkuAndEffectiveDate(
                    csvRecord.getStoreId(), csvRecord.getSku(), parsedDate);

            PricingRecord record = existingRecordOpt.orElse(new PricingRecord());

            record.setStoreId(csvRecord.getStoreId());
            record.setSku(csvRecord.getSku());
            record.setProductName(csvRecord.getProductName());
            record.setPrice(csvRecord.getPrice());
            record.setEffectiveDate(parsedDate);

            return record;
        };
    }

    // 3. WRITER: Writes chunks to the database
    @Bean
    public RepositoryItemWriter<PricingRecord> writer() {
        RepositoryItemWriter<PricingRecord> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    // 4. STEP: Tie Reader, Processor, and Writer together
    @Bean
    public Step processCsvStep() {
        return new StepBuilder("processCsvStep", jobRepository)
                .<CsvPricingRecord, PricingRecord>chunk(1000, transactionManager) // Process in chunks of 1000
                .reader(reader(null)) // Injected via @StepScope at runtime
                .processor(processor())
                .writer(writer())
                .build();
    }

    // 5. JOB: The overall batch job definition
    @Bean
    public Job importPricingJob() {
        return new JobBuilder("importPricingJob", jobRepository)
                .start(processCsvStep())
                .build();
    }
}