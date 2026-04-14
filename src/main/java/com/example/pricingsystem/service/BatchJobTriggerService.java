package com.example.pricingsystem.service;

import com.example.pricingsystem.event.FileUploadedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchJobTriggerService {

    private final JobLauncher jobLauncher;
    private final Job importPricingJob;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "pricing-feed-uploads", groupId = "pricing-batch-group")
    public void consumeFileUploadEvent(String messagePayload) {
        try {
            // Parse the JSON message from Kafka back into our Event Object
            FileUploadedEvent event = objectMapper.readValue(messagePayload, FileUploadedEvent.class);
            log.info("Received Kafka event to process file: {}", event.originalFileName());

            // Pass the file path as a Job Parameter to Spring Batch
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", event.filePath())
                    .addLong("time", System.currentTimeMillis()) // Ensure job uniqueness
                    .toJobParameters();

            // Launch the Batch Job
            jobLauncher.run(importPricingJob, jobParameters);

            log.info("Batch job launched successfully for file: {}", event.originalFileName());

        } catch (Exception e) {
            log.error("Failed to launch batch job from Kafka event", e);
            // In a production system, you would send this to a Dead Letter Queue (DLQ) here
        }
    }
}