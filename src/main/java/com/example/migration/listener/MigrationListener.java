package com.example.migration.listener;

import com.example.migration.model.Metadata;
import com.example.migration.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MigrationListener implements JobExecutionListener {

    private final MigrationService migrationService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("🚀 Starting Excel to JSON migration job...");
        log.info("📋 Job ID: {}", jobExecution.getId());
        log.info("🏷️ Job Name: {}", jobExecution.getJobInstance().getJobName());

        // Save initial metadata
        Metadata metadata = Metadata.builder()
                .fileName("excel-to-json-migration")
                .totalRecords(0)
                .processedRecords(0)
                .failedRecords(0)
                .startTime(LocalDateTime.now())
                .status("RUNNING")
                .outputPath("output/json")
                .build();

        migrationService.saveMetadata(metadata);

        // Store metadata ID in execution context for later update
        jobExecution.getExecutionContext().putLong("metadataId", metadata.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        log.info("🏁 Job completed with status: {}", status);

        // Get metadata ID from context
        Long metadataId = jobExecution.getExecutionContext().getLong("metadataId");
        Metadata metadata = migrationService.getMetadataById(metadataId);

        if (metadata != null) {
            metadata.setEndTime(LocalDateTime.now());
            metadata.setStatus(status.toString());

            // Get counts from execution context
            metadata.setProcessedRecords(
                    jobExecution.getExecutionContext().getInt("processedRecords", 0)
            );
            metadata.setFailedRecords(
                    jobExecution.getExecutionContext().getInt("failedRecords", 0)
            );
            metadata.setTotalRecords(
                    jobExecution.getExecutionContext().getInt("totalRecords", 0)
            );

            if (status == BatchStatus.FAILED) {
                metadata.setErrorMessage(
                        jobExecution.getExecutionContext().getString("errorMessage", "Unknown error")
                );
            }

            migrationService.saveMetadata(metadata);
            log.info("✅ Metadata updated successfully");
        }

        if (status == BatchStatus.COMPLETED) {
            log.info("🎉 Migration completed successfully!");
        } else {
            log.error("❌ Migration failed with status: {}", status);
        }
    }
}