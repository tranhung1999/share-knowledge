import apiClient from './api';
import {
  ApiResponse, DashboardSummary, ExpenseTypeData,
  MonthlyData, DepartmentData
} from '../types';

const reportService = {
  getDashboard: () =>
    apiClient.get<ApiResponse<DashboardSummary>>('/reports/dashboard').then(r => r.data),

  getByType: (startDate?: string, endDate?: string) =>
    apiClient.get<ApiResponse<ExpenseTypeData[]>>('/reports/by-type', {
      params: { startDate, endDate }
    }).then(r => r.data),

  getMonthly: (year?: number) =>
    apiClient.get<ApiResponse<MonthlyData[]>>('/reports/monthly', {
      params: { year }
    }).then(r => r.data),

  getByDepartment: (startDate?: string, endDate?: string) =>
    apiClient.get<ApiResponse<DepartmentData[]>>('/reports/by-department', {
      params: { startDate, endDate }
    }).then(r => r.data),

  exportExcel: (startDate?: string, endDate?: string) =>
    apiClient.get('/reports/export/excel', {
      params: { startDate, endDate },
      responseType: 'blob',
    }).then(r => r.data as Blob),
};

export default reportService;
