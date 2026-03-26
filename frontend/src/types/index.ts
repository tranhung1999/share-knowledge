// ─── Enums ───────────────────────────────────────────────────────────────────

export type ExpenseType =
  | 'TRAVEL' | 'ACCOMMODATION' | 'MEALS' | 'OFFICE_SUPPLIES'
  | 'TRAINING' | 'ENTERTAINMENT' | 'MEDICAL' | 'OTHER';

export type ExpenseStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'PAID';

export type UserRole = 'ADMIN' | 'MANAGER' | 'ACCOUNTANT' | 'EMPLOYEE';

export type NotificationType =
  | 'EXPENSE_SUBMITTED' | 'EXPENSE_APPROVED' | 'EXPENSE_REJECTED'
  | 'EXPENSE_PAID' | 'GENERAL';

// ─── User ────────────────────────────────────────────────────────────────────

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  department: string | null;
  departmentId: number | null;
  isActive: boolean;
  createdAt: string;
}

// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

// ─── Expense ─────────────────────────────────────────────────────────────────

export interface Attachment {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  downloadUrl: string;
  createdAt: string;
}

export interface ApprovalRecord {
  id: number;
  expenseId: number;
  approverName: string;
  approverEmail: string;
  action: 'APPROVE' | 'REJECT' | 'PAY';
  comment: string | null;
  createdAt: string;
}

export interface Expense {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  department: string | null;
  expenseType: ExpenseType;
  amount: number;
  expenseDate: string;
  description: string;
  status: ExpenseStatus;
  attachments: Attachment[];
  approvals: ApprovalRecord[];
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseFormData {
  expenseType: ExpenseType;
  amount: number;
  expenseDate: string;
  description: string;
}

// ─── Notifications ────────────────────────────────────────────────────────────

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  expenseId: number | null;
  isRead: boolean;
  createdAt: string;
}

// ─── Reports ─────────────────────────────────────────────────────────────────

export interface DashboardSummary {
  totalExpenses: number;
  draftCount: number;
  submittedCount: number;
  approvedCount: number;
  rejectedCount: number;
  paidCount: number;
  totalPaidAmount: number;
  currentMonthPaidAmount: number;
  topExpenseTypes: ExpenseTypeData[];
  last6MonthsTrend: MonthlyData[];
}

export interface ExpenseTypeData {
  expenseType: string;
  count: number;
  totalAmount: number;
}

export interface MonthlyData {
  year: number;
  month: number;
  monthName: string;
  count: number;
  totalAmount: number;
}

export interface DepartmentData {
  department: string;
  count: number;
  totalAmount: number;
}

// ─── API ─────────────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
