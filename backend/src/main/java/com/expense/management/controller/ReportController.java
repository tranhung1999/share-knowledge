package com.expense.management.controller;

import com.expense.management.dto.response.ApiResponse;
import com.expense.management.dto.response.ReportResponse.*;
import com.expense.management.service.ReportService;
import com.expense.management.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(Constants.API_REPORTS)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMIN')")
@Tag(name = "Reports", description = "Reporting and analytics endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary statistics")
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDashboardSummary()));
    }

    @GetMapping("/by-type")
    @Operation(summary = "Get expense breakdown by type")
    public ResponseEntity<ApiResponse<List<ExpenseTypeData>>> getByType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getExpenseByTypeReport(startDate, endDate)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly expense report")
    public ResponseEntity<ApiResponse<List<MonthlyData>>> getMonthly(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMonthlyReport(year)));
    }

    @GetMapping("/by-department")
    @Operation(summary = "Get expense breakdown by department")
    public ResponseEntity<ApiResponse<List<DepartmentData>>> getByDepartment(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                reportService.getDepartmentReport(startDate, endDate)));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export report to Excel (.xlsx)")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            throws IOException {
        byte[] data = reportService.exportToExcel(startDate, endDate);
        String filename = "expense_report_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}
