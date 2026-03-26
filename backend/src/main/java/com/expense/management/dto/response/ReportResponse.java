package com.expense.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ReportResponse {

    @Getter
    @Builder
    public static class ExpenseTypeData {
        private String expenseType;
        private long count;
        private BigDecimal totalAmount;
    }

    @Getter
    @Builder
    public static class MonthlyData {
        private int year;
        private int month;
        private String monthName;
        private long count;
        private BigDecimal totalAmount;
    }

    @Getter
    @Builder
    public static class DepartmentData {
        private String department;
        private long count;
        private BigDecimal totalAmount;
    }

    @Getter
    @Builder
    public static class DashboardSummary {
        private long totalExpenses;
        private long draftCount;
        private long submittedCount;
        private long approvedCount;
        private long rejectedCount;
        private long paidCount;
        private BigDecimal totalPaidAmount;
        private BigDecimal currentMonthPaidAmount;
        private List<ExpenseTypeData> topExpenseTypes;
        private List<MonthlyData> last6MonthsTrend;
    }
}
