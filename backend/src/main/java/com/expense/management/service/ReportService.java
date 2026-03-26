package com.expense.management.service;

import com.expense.management.dto.response.ReportResponse;
import com.expense.management.dto.response.ReportResponse.*;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary() {
        long total    = expenseRepository.count();
        long draft     = expenseRepository.countByStatus(ExpenseStatus.DRAFT);
        long submitted = expenseRepository.countByStatus(ExpenseStatus.SUBMITTED);
        long approved  = expenseRepository.countByStatus(ExpenseStatus.APPROVED);
        long rejected  = expenseRepository.countByStatus(ExpenseStatus.REJECTED);
        long paid      = expenseRepository.countByStatus(ExpenseStatus.PAID);

        LocalDate now = LocalDate.now();
        BigDecimal currentMonthPaid = expenseRepository.sumPaidAmountBetween(
                now.withDayOfMonth(1), now);

        // YTD paid
        BigDecimal totalPaid = expenseRepository.sumPaidAmountBetween(
                now.withDayOfYear(1), now);

        List<ExpenseTypeData> typeData = buildTypeData(null, null);
        List<MonthlyData> trend = buildLast6MonthsTrend();

        return DashboardSummary.builder()
                .totalExpenses(total)
                .draftCount(draft)
                .submittedCount(submitted)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .paidCount(paid)
                .totalPaidAmount(totalPaid)
                .currentMonthPaidAmount(currentMonthPaid)
                .topExpenseTypes(typeData)
                .last6MonthsTrend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExpenseTypeData> getExpenseByTypeReport(LocalDate startDate, LocalDate endDate) {
        return buildTypeData(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<MonthlyData> getMonthlyReport(Integer year) {
        List<Object[]> rows = expenseRepository.getMonthlyReport(year);
        List<MonthlyData> result = new ArrayList<>();
        for (Object[] row : rows) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            result.add(MonthlyData.builder()
                    .year(y)
                    .month(m)
                    .monthName(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .count(((Number) row[2]).longValue())
                    .totalAmount(toBigDecimal(row[3]))
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DepartmentData> getDepartmentReport(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = expenseRepository.getDepartmentReport(startDate, endDate);
        List<DepartmentData> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(DepartmentData.builder()
                    .department(row[0] != null ? row[0].toString() : "Unknown")
                    .count(((Number) row[1]).longValue())
                    .totalAmount(toBigDecimal(row[2]))
                    .build());
        }
        return result;
    }

    public byte[] exportToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            addSummarySheet(workbook, startDate, endDate);
            addTypeSheet(workbook, startDate, endDate);
            addDepartmentSheet(workbook, startDate, endDate);
            addMonthlySheet(workbook);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void addSummarySheet(XSSFWorkbook workbook, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet("Summary");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row titleRow = sheet.createRow(0);
        Cell title = titleRow.createCell(0);
        title.setCellValue("Expense Report Summary");
        title.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        Row dateRow = sheet.createRow(1);
        dateRow.createCell(0).setCellValue("Period:");
        dateRow.createCell(1).setCellValue(
                (startDate != null ? startDate : "All") + " to " +
                (endDate != null ? endDate : "All"));

        Row headerRow = sheet.createRow(3);
        String[] headers = {"Metric", "Count", "Total Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DashboardSummary summary = getDashboardSummary();
        String[][] data = {
            {"Total Expenses", String.valueOf(summary.getTotalExpenses()), ""},
            {"Draft", String.valueOf(summary.getDraftCount()), ""},
            {"Submitted", String.valueOf(summary.getSubmittedCount()), ""},
            {"Approved", String.valueOf(summary.getApprovedCount()), ""},
            {"Rejected", String.valueOf(summary.getRejectedCount()), ""},
            {"Paid", String.valueOf(summary.getPaidCount()),
                    summary.getTotalPaidAmount().toPlainString()},
        };

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(4 + i);
            for (int j = 0; j < data[i].length; j++) {
                row.createCell(j).setCellValue(data[i][j]);
            }
        }

        for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
    }

    private void addTypeSheet(XSSFWorkbook workbook, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet("By Type");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Expense Type", "Count", "Total Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<ExpenseTypeData> data = buildTypeData(startDate, endDate);
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(data.get(i).getExpenseType());
            row.createCell(1).setCellValue(data.get(i).getCount());
            row.createCell(2).setCellValue(data.get(i).getTotalAmount().doubleValue());
        }

        for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
    }

    private void addDepartmentSheet(XSSFWorkbook workbook, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet("By Department");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Department", "Count", "Total Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<DepartmentData> data = getDepartmentReport(startDate, endDate);
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(data.get(i).getDepartment());
            row.createCell(1).setCellValue(data.get(i).getCount());
            row.createCell(2).setCellValue(data.get(i).getTotalAmount().doubleValue());
        }

        for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
    }

    private void addMonthlySheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet("Monthly Trend");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Year", "Month", "Count", "Total Amount"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<MonthlyData> data = getMonthlyReport(null);
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(data.get(i).getYear());
            row.createCell(1).setCellValue(data.get(i).getMonthName());
            row.createCell(2).setCellValue(data.get(i).getCount());
            row.createCell(3).setCellValue(data.get(i).getTotalAmount().doubleValue());
        }

        for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
    }

    private List<ExpenseTypeData> buildTypeData(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = expenseRepository.getExpenseByTypeReport(startDate, endDate);
        List<ExpenseTypeData> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(ExpenseTypeData.builder()
                    .expenseType(row[0].toString())
                    .count(((Number) row[1]).longValue())
                    .totalAmount(toBigDecimal(row[2]))
                    .build());
        }
        return result;
    }

    private List<MonthlyData> buildLast6MonthsTrend() {
        LocalDate now = LocalDate.now();
        return getMonthlyReport(now.getYear()).stream()
                .filter(d -> d.getMonth() > now.getMonthValue() - 6)
                .toList();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
