# API Documentation — Expense Management System

**Base URL:** `http://localhost:8080/api/v1`
**Auth:** All endpoints (except `/auth/login` and `/health`) require `Authorization: Bearer <token>` header.

---

## Authentication

### POST `/auth/login`
Login with email and password.

**Request:**
```json
{
  "email": "alice@company.com",
  "password": "Employee@123"
}
```

**Response 200:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 4,
      "email": "alice@company.com",
      "fullName": "Alice Johnson",
      "role": "EMPLOYEE",
      "department": "Engineering",
      "departmentId": 1,
      "isActive": true,
      "createdAt": "2025-01-01T00:00:00"
    }
  }
}
```

**Response 401:**
```json
{ "success": false, "error": "Invalid email or password" }
```

---

## Expenses

### POST `/expenses`
Create a new expense (EMPLOYEE).

**Request:**
```json
{
  "expenseType": "TRAVEL",
  "amount": 250.00,
  "expenseDate": "2025-03-20",
  "description": "Round-trip flight to annual conference in NYC"
}
```

**Response 201:**
```json
{
  "success": true,
  "message": "Expense created",
  "data": {
    "id": 1,
    "employeeId": 4,
    "employeeName": "Alice Johnson",
    "expenseType": "TRAVEL",
    "amount": 250.00,
    "expenseDate": "2025-03-20",
    "description": "Round-trip flight to annual conference in NYC",
    "status": "DRAFT",
    "attachments": [],
    "approvals": [],
    "createdAt": "2025-03-26T10:00:00",
    "updatedAt": "2025-03-26T10:00:00"
  }
}
```

---

### PUT `/expenses/{id}`
Update a DRAFT expense.

### POST `/expenses/{id}/submit`
Submit a DRAFT expense for approval. Triggers notification to managers.

### DELETE `/expenses/{id}`
Delete a DRAFT expense.

### GET `/expenses/{id}`
Get expense details. Employee sees own, Manager/Accountant/Admin see all.

### GET `/expenses/my?status=DRAFT&page=0&size=20`
Paginated list of current user's expenses.

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "last": false
  }
}
```

### GET `/expenses?status=SUBMITTED&page=0&size=20`
All expenses with filters. Roles: MANAGER, ACCOUNTANT, ADMIN.

**Query Parameters:**
| Param        | Type   | Description                    |
|--------------|--------|--------------------------------|
| `status`     | enum   | DRAFT/SUBMITTED/APPROVED/...   |
| `employeeId` | long   | Filter by employee             |
| `expenseType`| enum   | TRAVEL/MEALS/etc.              |
| `startDate`  | date   | ISO 8601 (YYYY-MM-DD)          |
| `endDate`    | date   | ISO 8601 (YYYY-MM-DD)          |
| `page`       | int    | 0-indexed page number          |
| `size`       | int    | Page size (max 100)            |

---

## File Attachments

### POST `/expenses/{id}/attachments`
Upload a file attachment. Content-Type: `multipart/form-data`.

**Form field:** `file` (JPG/PNG/PDF, max 10MB)

### DELETE `/expenses/{id}/attachments/{attachmentId}`
Remove an attachment from a DRAFT expense.

### GET `/files/{attachmentId}`
Download a file. Returns binary with appropriate Content-Type.

---

## Approvals

### POST `/approvals/{expenseId}/approve`
Approve a SUBMITTED expense. Roles: MANAGER, ADMIN.

**Request (optional):**
```json
{ "comment": "Approved — within policy limits" }
```

**Response 200:**
```json
{
  "success": true,
  "message": "Expense approved",
  "data": { ...expense with status: "APPROVED"... }
}
```

### POST `/approvals/{expenseId}/reject`
Reject a SUBMITTED expense. Roles: MANAGER, ADMIN.

**Request (optional):**
```json
{ "comment": "Amount exceeds $500 single-trip limit" }
```

### POST `/approvals/{expenseId}/pay`
Mark an APPROVED expense as PAID. Roles: ACCOUNTANT, ADMIN.

---

## Reports

> All report endpoints require role: MANAGER, ACCOUNTANT, or ADMIN.

### GET `/reports/dashboard`
Full dashboard summary statistics.

**Response:**
```json
{
  "data": {
    "totalExpenses": 128,
    "draftCount": 12,
    "submittedCount": 8,
    "approvedCount": 15,
    "rejectedCount": 5,
    "paidCount": 88,
    "totalPaidAmount": 47250.00,
    "currentMonthPaidAmount": 3800.00,
    "topExpenseTypes": [
      { "expenseType": "TRAVEL", "count": 34, "totalAmount": 18200.00 },
      { "expenseType": "MEALS",  "count": 28, "totalAmount": 4100.00 }
    ],
    "last6MonthsTrend": [
      { "year": 2025, "month": 1, "monthName": "January", "count": 18, "totalAmount": 7200.00 }
    ]
  }
}
```

### GET `/reports/by-type?startDate=2025-01-01&endDate=2025-03-26`
Breakdown by expense type.

### GET `/reports/monthly?year=2025`
Monthly breakdown for a given year.

### GET `/reports/by-department?startDate=2025-01-01&endDate=2025-03-26`
Breakdown by department.

### GET `/reports/export/excel?startDate=2025-01-01&endDate=2025-03-26`
Download Excel report (`.xlsx`). Returns binary.

---

## Notifications

### GET `/notifications?page=0&size=20`
Get paginated notifications for current user.

### GET `/notifications/unread-count`
Get count of unread notifications.

**Response:** `{ "data": { "unreadCount": 3 } }`

### PATCH `/notifications/{id}/read`
Mark a single notification as read.

### PATCH `/notifications/read-all`
Mark all notifications as read.

---

## Users

### GET `/users/me`
Get current user's profile.

### GET `/users`
List all users. Role: ADMIN only.

### POST `/users`
Create a new user. Role: ADMIN only.

**Request:**
```json
{
  "email": "newuser@company.com",
  "password": "Password@123",
  "fullName": "New Employee",
  "departmentId": 1,
  "roleName": "EMPLOYEE"
}
```

### PATCH `/users/{id}/status?active=false`
Activate or deactivate a user. Role: ADMIN only.

---

## Error Responses

All error responses follow the same structure:

```json
{
  "success": false,
  "error": "Human-readable error message",
  "timestamp": "2025-03-26T10:00:00"
}
```

**Validation errors include field-level details:**
```json
{
  "success": false,
  "error": "Validation failed",
  "data": {
    "amount": "Amount must be greater than 0",
    "expenseDate": "Expense date cannot be in the future"
  }
}
```

| HTTP Status | Meaning                                        |
|-------------|------------------------------------------------|
| 200         | Success                                        |
| 201         | Created                                        |
| 400         | Bad request / validation error                 |
| 401         | Unauthenticated (missing or invalid token)     |
| 403         | Forbidden (insufficient role)                  |
| 404         | Resource not found                             |
| 500         | Internal server error (no stack trace exposed) |
