package com.example.pricingsystem.service;

import com.example.pricingsystem.event.FileUploadedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedIngestionService {

    // 1. Change to String, String
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 2. Inject ObjectMapper to convert objects to JSON
    private final ObjectMapper objectMapper;

    private static final String KAFKA_TOPIC = "pricing-feed-uploads";
    private static final String STORAGE_DIR = System.getProperty("java.io.tmpdir") + "/pricing-uploads/";

    public String acceptFile(MultipartFile file) {
        try {
            File directory = new File(STORAGE_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(STORAGE_DIR + uniqueFileName);
            Files.write(filePath, file.getBytes());

            // 3. Create the event, convert to JSON String, and send
            FileUploadedEvent event = new FileUploadedEvent(filePath.toString(), file.getOriginalFilename());
            String jsonMessage = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(KAFKA_TOPIC, jsonMessage);

            log.info("File saved and event published to Kafka: {}", uniqueFileName);
            return uniqueFileName;

        } catch (IOException e) {
            log.error("Failed to store file or send to Kafka", e);
            throw new RuntimeException("Could not process the file. Error: " + e.getMessage());
        }
    }
}