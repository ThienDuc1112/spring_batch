package com.example.migration.processor;

import com.example.migration.model.ExcelData;
import com.example.migration.model.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class DataProcessor implements ItemProcessor<ExcelData, JsonData> {

    private final ObjectMapper objectMapper;
    private String fileName;

    public DataProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.fileName = stepExecution.getExecutionContext().getString("fileName", "unknown");
    }

    @Override
    public JsonData process(ExcelData item) throws Exception {
        if (item == null) {
            return null;
        }

        // Validate data
        String validationErrors = validateData(item);

        // Convert ExcelData to Map
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", item.getId());
        dataMap.put("name", item.getName());
        dataMap.put("email", item.getEmail());
        dataMap.put("phone", item.getPhone());
        dataMap.put("address", item.getAddress());
        dataMap.put("department", item.getDepartment());
        dataMap.put("salary", item.getSalary());
        dataMap.put("joinDate", item.getJoinDate());
        dataMap.put("isActive", item.getIsActive());
        dataMap.put("notes", item.getNotes());

        // Build JSON data
        JsonData jsonData = JsonData.builder()
                .documentId(UUID.randomUUID().toString())
                .sourceFile(fileName)
                .sourceRowId(item.getId() != null ? item.getId() : 0L)
                .processedAt(LocalDateTime.now())
                .data(dataMap)
                .status(validationErrors.isEmpty() ? "SUCCESS" : "WARNING")
                .validationErrors(validationErrors)
                .build();

        log.debug("🔄 Processed record: {}", jsonData.getDocumentId());
        return jsonData;
    }

    private String validateData(ExcelData item) {
        StringBuilder errors = new StringBuilder();

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            errors.append("Name is required; ");
        }

        if (item.getEmail() != null && !item.getEmail().contains("@")) {
            errors.append("Invalid email format; ");
        }

        if (item.getSalary() != null && item.getSalary() < 0) {
            errors.append("Salary cannot be negative; ");
        }

        return errors.toString();
    }
}