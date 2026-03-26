package com.expense.management.repository;

import com.expense.management.entity.ExpenseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, Long> {
    List<ExpenseAttachment> findByExpenseId(Long expenseId);
    void deleteByExpenseId(Long expenseId);
}
