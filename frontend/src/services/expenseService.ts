import apiClient from './api';
import {
  ApiResponse, PageResponse, Expense, ExpenseFormData,
  ExpenseStatus, ExpenseType
} from '../types';

export interface ExpenseFilters {
  status?: ExpenseStatus;
  employeeId?: number;
  expenseType?: ExpenseType;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

const expenseService = {
  createExpense: (data: ExpenseFormData) =>
    apiClient.post<ApiResponse<Expense>>('/expenses', data).then(r => r.data),

  updateExpense: (id: number, data: ExpenseFormData) =>
    apiClient.put<ApiResponse<Expense>>(`/expenses/${id}`, data).then(r => r.data),

  submitExpense: (id: number) =>
    apiClient.post<ApiResponse<Expense>>(`/expenses/${id}/submit`).then(r => r.data),

  deleteExpense: (id: number) =>
    apiClient.delete<ApiResponse<void>>(`/expenses/${id}`).then(r => r.data),

  getExpenseById: (id: number) =>
    apiClient.get<ApiResponse<Expense>>(`/expenses/${id}`).then(r => r.data),

  getMyExpenses: (filters: ExpenseFilters = {}) =>
    apiClient.get<ApiResponse<PageResponse<Expense>>>('/expenses/my', {
      params: filters
    }).then(r => r.data),

  getAllExpenses: (filters: ExpenseFilters = {}) =>
    apiClient.get<ApiResponse<PageResponse<Expense>>>('/expenses', {
      params: filters
    }).then(r => r.data),

  uploadAttachment: (id: number, file: File) => {
    const form = new FormData();
    form.append('file', file);
    return apiClient.post<ApiResponse<Expense>>(`/expenses/${id}/attachments`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(r => r.data);
  },

  deleteAttachment: (expenseId: number, attachmentId: number) =>
    apiClient.delete<ApiResponse<void>>(
      `/expenses/${expenseId}/attachments/${attachmentId}`
    ).then(r => r.data),
};

export default expenseService;
