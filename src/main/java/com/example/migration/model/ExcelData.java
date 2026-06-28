package com.example.migration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelData {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String department;
    private Double salary;
    private LocalDateTime joinDate;
    private Boolean isActive;
    private String notes;
}