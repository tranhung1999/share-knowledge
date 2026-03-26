package com.expense.management.repository;

import com.expense.management.entity.Approval;
import com.expense.management.entity.Approval.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByExpenseIdOrderByCreatedAtDesc(Long expenseId);
    List<Approval> findByApproverIdOrderByCreatedAtDesc(Long approverId);
    boolean existsByExpenseIdAndAction(Long expenseId, ApprovalAction action);
}
