package com.example.migration.config;

import com.example.migration.model.ExcelData;
import com.example.migration.model.JsonData;
import com.example.migration.processor.DataProcessor;
import com.example.migration.reader.ExcelReader;
import com.example.migration.writer.JsonWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExcelReader excelReader;
    private final DataProcessor dataProcessor;
    private final JsonWriter jsonWriter;

    @Value("${migration.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Job excelToJsonMigrationJob() {
        return new JobBuilder("excelToJsonMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(excelToJsonStep())
                .build();
    }

    @Bean
    public Step excelToJsonStep() {
        return new StepBuilder("excelToJsonStep", jobRepository)
                .<ExcelData, JsonData>chunk(chunkSize)
                .reader(excelReader)
                .processor(dataProcessor)
                .writer(jsonWriter)
                .transactionManager(transactionManager)
                .build();
    }
}