package com.example.pricingsystem.event;

public record FileUploadedEvent(String filePath, String originalFileName) {
}