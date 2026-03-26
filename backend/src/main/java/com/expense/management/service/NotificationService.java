package com.expense.management.service;

import com.expense.management.dto.response.NotificationResponse;
import com.expense.management.dto.response.PageResponse;
import com.expense.management.entity.Expense;
import com.expense.management.entity.Notification;
import com.expense.management.entity.Notification.NotificationType;
import com.expense.management.entity.User;
import com.expense.management.exception.ResourceNotFoundException;
import com.expense.management.repository.NotificationRepository;
import com.expense.management.repository.UserRepository;
import com.expense.management.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async
    public void notifyExpenseSubmitted(Expense expense) {
        // Notify all managers
        List<User> managers = userRepository.findActiveUsersByRole(Constants.ROLE_MANAGER);
        String title = "New Expense Submitted";
        String message = String.format("%s submitted a %s expense for $%.2f",
                expense.getEmployee().getFullName(),
                expense.getExpenseType().name(),
                expense.getAmount());

        managers.forEach(manager -> createNotification(
                manager, expense, title, message, NotificationType.EXPENSE_SUBMITTED));

        log.info("Notified {} managers about expense #{}", managers.size(), expense.getId());
    }

    @Async
    public void notifyExpenseApproved(Expense expense) {
        String title = "Expense Approved";
        String message = String.format("Your %s expense of $%.2f has been approved",
                expense.getExpenseType().name(), expense.getAmount());
        createNotification(expense.getEmployee(), expense, title, message,
                NotificationType.EXPENSE_APPROVED);
    }

    @Async
    public void notifyExpenseRejected(Expense expense, String comment) {
        String title = "Expense Rejected";
        String message = String.format("Your %s expense of $%.2f has been rejected. Reason: %s",
                expense.getExpenseType().name(), expense.getAmount(),
                comment != null ? comment : "No reason provided");
        createNotification(expense.getEmployee(), expense, title, message,
                NotificationType.EXPENSE_REJECTED);
    }

    @Async
    public void notifyExpensePaid(Expense expense) {
        String title = "Expense Paid";
        String message = String.format("Your %s expense of $%.2f has been paid",
                expense.getExpenseType().name(), expense.getAmount());
        createNotification(expense.getEmployee(), expense, title, message,
                NotificationType.EXPENSE_PAID);

        // Also notify managers
        List<User> accountants = userRepository.findActiveUsersByRole(Constants.ROLE_ACCOUNTANT);
        // (already acted, this is just confirmation logging)
        log.info("Expense #{} marked as paid", expense.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE)));
        return PageResponse.from(notifications.map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification", notificationId);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    private void createNotification(User user, Expense expense, String title,
                                    String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .expense(expense)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }
}
