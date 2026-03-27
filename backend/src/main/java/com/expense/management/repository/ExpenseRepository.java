package com.expense.management.repository;

import com.expense.management.entity.Expense;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.Expense.ExpenseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<Expense> findByEmployeeIdAndStatus(Long employeeId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByStatus(ExpenseStatus status, Pageable pageable);

    @Query("""
        SELECT e FROM Expense e
        WHERE (:status IS NULL OR e.status = :status)
          AND (:employeeId IS NULL OR e.employee.id = :employeeId)
          AND (:expenseType IS NULL OR e.expenseType = :expenseType)
          AND (:startDate IS NULL OR e.expenseDate >= :startDate)
          AND (:endDate IS NULL OR e.expenseDate <= :endDate)
        """)
    Page<Expense> findWithFilters(
            @Param("status") ExpenseStatus status,
            @Param("employeeId") Long employeeId,
            @Param("expenseType") ExpenseType expenseType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Report queries
    @Query("""
        SELECT e.expenseType, COUNT(e), SUM(e.amount)
        FROM Expense e
        WHERE e.status = 'PAID'
          AND e.expenseDate >= :startDate
          AND e.expenseDate <= :endDate
        GROUP BY e.expenseType
        ORDER BY SUM(e.amount) DESC
        """)
    List<Object[]> getExpenseByTypeReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate),
               COUNT(e), SUM(e.amount)
        FROM Expense e
        WHERE e.status = 'PAID'
          AND EXTRACT(YEAR FROM e.expenseDate) = :year
        GROUP BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)
        ORDER BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)
        """)
    List<Object[]> getMonthlyReport(@Param("year") int year);

    @Query("""
        SELECT EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate),
               COUNT(e), SUM(e.amount)
        FROM Expense e
        WHERE e.status = 'PAID'
        GROUP BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)
        ORDER BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)
        """)
    List<Object[]> getMonthlyReportAllYears();

    @Query("""
        SELECT u.department.name, COUNT(e), SUM(e.amount)
        FROM Expense e
        JOIN e.employee u
        WHERE e.status = 'PAID'
          AND e.expenseDate >= :startDate
          AND e.expenseDate <= :endDate
        GROUP BY u.department.name
        ORDER BY SUM(e.amount) DESC
        """)
    List<Object[]> getDepartmentReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COUNT(e) FROM Expense e WHERE e.status = :status
        """)
    Long countByStatus(@Param("status") ExpenseStatus status);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0) FROM Expense e
        WHERE e.status = 'PAID'
          AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate
        """)
    BigDecimal sumPaidAmountBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
