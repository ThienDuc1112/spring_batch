package com.example.migration.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Random;

public class ExcelGenerator {

    public static void main(String[] args) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        String[] headers = {
                "id", "name", "email", "phone", "address",
                "department", "salary", "joinDate", "isActive", "notes"
        };

        // HEADER
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        Random random = new Random();
        String[] departments = {"IT", "HR", "Finance", "Marketing", "Sales"};
        String[] cities = {"Hue", "Da Nang", "Hanoi", "HCMC", "Quang Tri"};

        // DATA 1000 rows
        for (int i = 1; i <= 1000; i++) {
            Row row = sheet.createRow(i);

            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue("User " + i);
            row.createCell(2).setCellValue("user" + i + "@gmail.com");
            row.createCell(3).setCellValue("09" + (10000000 + random.nextInt(89999999)));
            row.createCell(4).setCellValue(cities[random.nextInt(cities.length)]);
            row.createCell(5).setCellValue(departments[random.nextInt(departments.length)]);
            row.createCell(6).setCellValue(800 + random.nextInt(4200));
            row.createCell(7).setCellValue(LocalDateTime.now().minusDays(random.nextInt(1500)).toString());
            row.createCell(8).setCellValue(random.nextBoolean());
            row.createCell(9).setCellValue("Generated data");
        }

        try (FileOutputStream out = new FileOutputStream("sample-data.xlsx")) {
            workbook.write(out);
        }

        workbook.close();

        System.out.println("DONE -> sample-data-1000.xlsx created");
    }
}