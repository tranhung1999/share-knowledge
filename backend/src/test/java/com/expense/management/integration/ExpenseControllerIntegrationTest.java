package com.expense.management.integration;

import com.expense.management.dto.request.ExpenseRequest;
import com.expense.management.entity.Expense.ExpenseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Expense Controller Integration Tests")
class ExpenseControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /health should return OK without authentication")
    void health_endpoint_returnsOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.service").value("expense-management"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid credentials returns 401")
    void login_invalidCredentials_returns401() throws Exception {
        String body = "{\"email\":\"wrong@example.com\",\"password\":\"WrongPass1\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with blank email returns 400")
    void login_blankEmail_returns400() throws Exception {
        String body = "{\"email\":\"\",\"password\":\"Password1\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/expenses without token returns 401")
    void getExpenses_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/expenses without token returns 401")
    void createExpense_noToken_returns401() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setExpenseType(ExpenseType.TRAVEL);
        request.setAmount(new BigDecimal("100.00"));
        request.setExpenseDate(LocalDate.now());
        request.setDescription("Test expense");

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid JSON format returns 400")
    void login_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notanemail\",\"password\":\"pw\"}"))
                .andExpect(status().isBadRequest());
    }
}
