package com.expense.management.util;

public final class Constants {

    private Constants() {}

    // Roles
    public static final String ROLE_ADMIN      = "ADMIN";
    public static final String ROLE_MANAGER    = "MANAGER";
    public static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    public static final String ROLE_EMPLOYEE   = "EMPLOYEE";

    // File upload
    public static final long   MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB
    public static final String[] ALLOWED_FILE_TYPES = {"image/jpeg", "image/png", "application/pdf"};
    public static final String[] ALLOWED_EXTENSIONS  = {"jpg", "jpeg", "png", "pdf"};

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE     = 100;

    // API paths
    public static final String API_BASE      = "/api/v1";
    public static final String API_AUTH      = API_BASE + "/auth";
    public static final String API_EXPENSES  = API_BASE + "/expenses";
    public static final String API_APPROVALS = API_BASE + "/approvals";
    public static final String API_REPORTS   = API_BASE + "/reports";
    public static final String API_FILES     = API_BASE + "/files";
    public static final String API_USERS     = API_BASE + "/users";
    public static final String API_NOTIFICATIONS = API_BASE + "/notifications";

    // Auth
    public static final String BEARER_PREFIX         = "Bearer ";
    public static final String AUTHORIZATION_HEADER  = "Authorization";

    // Audit actions
    public static final String AUDIT_CREATE   = "CREATE";
    public static final String AUDIT_UPDATE   = "UPDATE";
    public static final String AUDIT_DELETE   = "DELETE";
    public static final String AUDIT_SUBMIT   = "SUBMIT";
    public static final String AUDIT_APPROVE  = "APPROVE";
    public static final String AUDIT_REJECT   = "REJECT";
    public static final String AUDIT_PAY      = "PAY";
    public static final String AUDIT_LOGIN    = "LOGIN";
    public static final String AUDIT_UPLOAD   = "UPLOAD";
}
