package com.expense.management.service;

import com.expense.management.dto.request.ExpenseRequest;
import com.expense.management.dto.response.ExpenseResponse;
import com.expense.management.dto.response.PageResponse;
import com.expense.management.entity.Expense;
import com.expense.management.entity.Expense.ExpenseStatus;
import com.expense.management.entity.Expense.ExpenseType;
import com.expense.management.entity.ExpenseAttachment;
import com.expense.management.entity.User;
import com.expense.management.exception.BusinessException;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.ExpenseAttachmentRepository;
import com.expense.management.repository.ExpenseRepository;
import com.expense.management.repository.UserRepository;
import com.expense.management.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));

        Expense expense = Expense.builder()
                .employee(employee)
                .expenseType(request.getExpenseType())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .status(ExpenseStatus.DRAFT)
                .build();

        Expense saved = expenseRepository.save(expense);
        auditLogService.log(employee, Constants.AUDIT_CREATE, "Expense", saved.getId(),
                null, toJson(saved), null);
        return ExpenseResponse.from(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request, Long employeeId) {
        Expense expense = getExpenseOwnedBy(expenseId, employeeId);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Only DRAFT expenses can be edited");
        }

        expense.setExpenseType(request.getExpenseType());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse submitExpense(Long expenseId, Long employeeId) {
        Expense expense = getExpenseOwnedBy(expenseId, employeeId);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Only DRAFT expenses can be submitted");
        }

        expense.setStatus(ExpenseStatus.SUBMITTED);
        Expense saved = expenseRepository.save(expense);

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));
        auditLogService.log(employee, Constants.AUDIT_SUBMIT, "Expense", expenseId,
                "DRAFT", "SUBMITTED", null);
        notificationService.notifyExpenseSubmitted(saved);

        return ExpenseResponse.from(saved);
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long employeeId) {
        Expense expense = getExpenseOwnedBy(expenseId, employeeId);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Only DRAFT expenses can be deleted");
        }
        // Delete associated files
        expense.getAttachments().forEach(att -> fileStorageService.deleteFile(att.getFilePath()));
        expenseRepository.delete(expense);
    }

    @Transactional
    public ExpenseResponse attachFile(Long expenseId, Long employeeId, MultipartFile file) {
        Expense expense = getExpenseOwnedBy(expenseId, employeeId);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Files can only be attached to DRAFT expenses");
        }

        String storedPath = fileStorageService.storeFile(file);
        ExpenseAttachment attachment = ExpenseAttachment.builder()
                .expense(expense)
                .fileName(file.getOriginalFilename())
                .filePath(storedPath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        attachmentRepository.save(attachment);

        Expense refreshed = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        return ExpenseResponse.from(refreshed);
    }

    @Transactional
    public void deleteAttachment(Long expenseId, Long attachmentId, Long employeeId) {
        getExpenseOwnedBy(expenseId, employeeId);
        ExpenseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
        if (!attachment.getExpense().getId().equals(expenseId)) {
            throw new BusinessException("Attachment does not belong to this expense",
                    HttpStatus.BAD_REQUEST);
        }
        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId, Long userId, String userRole) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));

        boolean isOwner = expense.getEmployee().getId().equals(userId);
        boolean isPrivileged = userRole.equals(Constants.ROLE_MANAGER)
                || userRole.equals(Constants.ROLE_ACCOUNTANT)
                || userRole.equals(Constants.ROLE_ADMIN);

        if (!isOwner && !isPrivileged) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }
        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getMyExpenses(Long employeeId, ExpenseStatus status,
                                                        int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Expense> expenses = status != null
                ? expenseRepository.findByEmployeeIdAndStatus(employeeId, status, pageable)
                : expenseRepository.findByEmployeeId(employeeId, pageable);
        return PageResponse.from(expenses.map(ExpenseResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getAllExpenses(ExpenseStatus status, Long employeeId,
                                                         ExpenseType expenseType,
                                                         LocalDate startDate, LocalDate endDate,
                                                         int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Expense> expenses = expenseRepository.findWithFilters(
                status, employeeId, expenseType, startDate, endDate, pageable);
        return PageResponse.from(expenses.map(ExpenseResponse::from));
    }

    private Expense getExpenseOwnedBy(Long expenseId, Long employeeId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        if (!expense.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException("You don't have permission to access this expense",
                    HttpStatus.FORBIDDEN);
        }
        return expense;
    }

    private String toJson(Expense expense) {
        return String.format("{\"id\":%d,\"status\":\"%s\",\"amount\":%s}",
                expense.getId(), expense.getStatus(), expense.getAmount());
    }
}
