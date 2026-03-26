import apiClient from './api';
import { ApiResponse, Expense } from '../types';

const approvalService = {
  approveExpense: (expenseId: number, comment?: string) =>
    apiClient.post<ApiResponse<Expense>>(`/approvals/${expenseId}/approve`, { comment })
      .then(r => r.data),

  rejectExpense: (expenseId: number, comment?: string) =>
    apiClient.post<ApiResponse<Expense>>(`/approvals/${expenseId}/reject`, { comment })
      .then(r => r.data),

  markAsPaid: (expenseId: number, comment?: string) =>
    apiClient.post<ApiResponse<Expense>>(`/approvals/${expenseId}/pay`, { comment })
      .then(r => r.data),
};

export default approvalService;
