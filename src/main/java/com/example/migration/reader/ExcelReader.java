package com.example.migration.reader;

import com.example.migration.model.ExcelData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.NonTransientResourceException;
import org.springframework.batch.infrastructure.item.ParseException;
import org.springframework.batch.infrastructure.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class ExcelReader implements ItemReader<ExcelData>, StepExecutionListener {

    @Value("${migration.input.file-path:input/sample-data.xlsx}")
    private String filePath;

    @Value("${migration.input.sheet-name:Sheet1}")
    private String sheetName;

    @Value("${migration.chunk-size:10}")
    private int chunkSize;

    private List<ExcelData> dataList;
    private Iterator<ExcelData> iterator;
    private int currentIndex = 0;
    private int totalRecords = 0;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("📖 Starting Excel reader: {}", filePath);
        try {
            loadExcelData();
            log.info("✅ Loaded {} records from Excel file", dataList.size());

            // Update metadata in execution context
            stepExecution.getExecutionContext().put("totalRecords", totalRecords);
            stepExecution.getExecutionContext().put("fileName", filePath);
        } catch (Exception e) {
            log.error("❌ Error loading Excel file: {}", e.getMessage());
            stepExecution.setExitStatus(ExitStatus.FAILED);
        }
    }

    @Override
    public ExcelData read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (iterator != null && iterator.hasNext()) {
            ExcelData data = iterator.next();
            currentIndex++;

            // Log progress every 100 records
            if (currentIndex % 100 == 0) {
                log.info("📊 Read {} records so far", currentIndex);
            }

            return data;
        }
        return null; // End of data
    }

    private void loadExcelData() {
        dataList = new ArrayList<>();

        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found");
            }

            // Get header row to map columns
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Header row not found");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            log.info("📋 Headers found: {}", headers);

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    ExcelData data = mapRowToExcelData(row, headers, i);
                    if (data != null) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error processing row {}: {}", i + 1, e.getMessage());
                }
            }

            totalRecords = dataList.size();
            iterator = dataList.iterator();

        } catch (Exception e) {
            log.error("❌ Failed to load Excel data: {}", e.getMessage());
            throw new RuntimeException("Error loading Excel file", e);
        }
    }

    private ExcelData mapRowToExcelData(Row row, List<String> headers, int rowIndex) {
        ExcelData.ExcelDataBuilder builder = ExcelData.builder();

        // Map based on column index
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            String header = headers.get(i).toLowerCase().trim();
            String value = getCellValueAsString(cell);

            switch (header) {
                case "id":
                    builder.id(value != null ? Long.valueOf(value) : null);
                    break;
                case "name":
                    builder.name(value);
                    break;
                case "email":
                    builder.email(value);
                    break;
                case "phone":
                    builder.phone(value);
                    break;
                case "address":
                    builder.address(value);
                    break;
                case "department":
                    builder.department(value);
                    break;
                case "salary":
                    builder.salary(value != null ? Double.valueOf(value) : null);
                    break;
                case "joindate":
                case "join_date":
                    builder.joinDate(value != null ? LocalDateTime.parse(value) : null);
                    break;
                case "isactive":
                case "active":
                    builder.isActive(value != null ? Boolean.valueOf(value) : null);
                    break;
                case "notes":
                    builder.notes(value);
                    break;
                default:
                    break;
            }
        }

        return builder.build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }

                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("📖 Excel reader completed. Total records read: {}", totalRecords);
        return stepExecution.getExitStatus();
    }
}