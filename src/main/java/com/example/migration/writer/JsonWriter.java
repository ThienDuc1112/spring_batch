package com.example.migration.writer;

import com.example.migration.model.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JsonWriter implements ItemWriter<JsonData> {

    @Value("${migration.output.directory:output/json}")
    private String outputDirectory;

    @Value("${migration.output.file-prefix:migrated-data}")
    private String filePrefix;

    private final ObjectMapper objectMapper;
    private final List<JsonData> allRecords = new ArrayList<>();
    private String outputFileName;
    private int totalRecords = 0;
    private StepExecution stepExecution;

    public JsonWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        createOutputDirectory();

        // Tạo tên file duy nhất cho toàn bộ job
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        outputFileName = String.format("%s_%s.json", filePrefix, timestamp);

        log.info("📁 Output file: {}/{}", outputDirectory, outputFileName);
    }

    @Override
    public void write(Chunk<? extends JsonData> chunk) throws Exception {
        if (chunk == null || chunk.getItems().isEmpty()) {
            return;
        }

        // Gom tất cả records vào list
        allRecords.addAll(chunk.getItems());
        totalRecords += chunk.getItems().size();

        log.info("📝 Collected {} records, total so far: {}", chunk.getItems().size(), totalRecords);

        // Cập nhật execution context
        if (stepExecution != null) {
            stepExecution.getExecutionContext().putInt("processedRecords", totalRecords);
        }
    }

    // Ghi tất cả sau khi step hoàn thành
    public void flush() throws IOException {
        if (!allRecords.isEmpty()) {
            Path filePath = Paths.get(outputDirectory, outputFileName);
            objectMapper.writeValue(filePath.toFile(), allRecords);
            log.info("✅ Successfully wrote all {} records to {}", totalRecords, outputFileName);
        }
    }

    private void createOutputDirectory() {
        try {
            Path path = Paths.get(outputDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("📁 Created output directory: {}", outputDirectory);
            }
        } catch (IOException e) {
            log.error("❌ Failed to create output directory: {}", e.getMessage());
            throw new RuntimeException("Failed to create output directory", e);
        }
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            flush();
            log.info("✅ Step completed. Total records written: {}", totalRecords);
        } catch (IOException e) {
            log.error("❌ Failed to flush data to file: {}", e.getMessage());
            return ExitStatus.FAILED;
        }
        return ExitStatus.COMPLETED;
    }
}