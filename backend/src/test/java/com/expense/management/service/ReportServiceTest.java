package com.expense.management.service;

import com.expense.management.dto.response.ReportResponse.*;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.repository.ExpenseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceTest {

    @Mock private ExpenseRepository expenseRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("getDashboardSummary: should aggregate counts from repository")
    void getDashboardSummary_returnsAggregatedData() {
        when(expenseRepository.count()).thenReturn(50L);
        when(expenseRepository.countByStatus(ExpenseStatus.DRAFT)).thenReturn(10L);
        when(expenseRepository.countByStatus(ExpenseStatus.SUBMITTED)).thenReturn(8L);
        when(expenseRepository.countByStatus(ExpenseStatus.APPROVED)).thenReturn(12L);
        when(expenseRepository.countByStatus(ExpenseStatus.REJECTED)).thenReturn(5L);
        when(expenseRepository.countByStatus(ExpenseStatus.PAID)).thenReturn(15L);
        when(expenseRepository.sumPaidAmountBetween(any(), any()))
                .thenReturn(new BigDecimal("12500.00"))
                .thenReturn(new BigDecimal("87000.00"));
        when(expenseRepository.getExpenseByTypeReport(any(), any())).thenReturn(List.of());
        when(expenseRepository.getMonthlyReport(any())).thenReturn(List.of());

        DashboardSummary summary = reportService.getDashboardSummary();

        assertThat(summary.getTotalExpenses()).isEqualTo(50L);
        assertThat(summary.getDraftCount()).isEqualTo(10L);
        assertThat(summary.getSubmittedCount()).isEqualTo(8L);
        assertThat(summary.getApprovedCount()).isEqualTo(12L);
        assertThat(summary.getRejectedCount()).isEqualTo(5L);
        assertThat(summary.getPaidCount()).isEqualTo(15L);
    }

    @Test
    @DisplayName("getExpenseByTypeReport: should map repository result to DTO")
    void getByTypeReport_mapsCorrectly() {
        Object[] row = {"TRAVEL", 5L, new BigDecimal("1250.00")};
        when(expenseRepository.getExpenseByTypeReport(any(), any()))
                .thenReturn(List.<Object[]>of(row));

        List<ExpenseTypeData> result = reportService.getExpenseByTypeReport(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExpenseType()).isEqualTo("TRAVEL");
        assertThat(result.get(0).getCount()).isEqualTo(5L);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("1250.00");
    }

    @Test
    @DisplayName("getDepartmentReport: should map repository result to DTO")
    void getDepartmentReport_mapsCorrectly() {
        Object[] row = {"Engineering", 8L, new BigDecimal("3200.00")};
        when(expenseRepository.getDepartmentReport(any(), any()))
                .thenReturn(List.<Object[]>of(row));

        List<DepartmentData> result = reportService.getDepartmentReport(
                LocalDate.of(2025, 1, 1), LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartment()).isEqualTo("Engineering");
        assertThat(result.get(0).getCount()).isEqualTo(8L);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("3200.00");
    }

    @Test
    @DisplayName("getMonthlyReport: should map year/month correctly")
    void getMonthlyReport_mapsCorrectly() {
        Object[] row = {2025.0, 3.0, 12L, new BigDecimal("5500.00")};
        when(expenseRepository.getMonthlyReport(2025)).thenReturn(List.<Object[]>of(row));

        List<MonthlyData> result = reportService.getMonthlyReport(2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getYear()).isEqualTo(2025);
        assertThat(result.get(0).getMonth()).isEqualTo(3);
        assertThat(result.get(0).getMonthName()).isEqualTo("March");
        assertThat(result.get(0).getCount()).isEqualTo(12L);
    }

    @Test
    @DisplayName("getExpenseByTypeReport: should return empty list when no data")
    void getByTypeReport_emptyData_returnsEmptyList() {
        when(expenseRepository.getExpenseByTypeReport(any(), any())).thenReturn(List.of());

        List<ExpenseTypeData> result = reportService.getExpenseByTypeReport(null, null);

        assertThat(result).isEmpty();
    }
}
