package com.example.minibank.controller;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.LoginRequest;
import com.example.minibank.dto.RegisterRequest;
import com.example.minibank.dto.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String accountNumber;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("ayman", "ayman@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest("ayman@test.com", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        AccountRequest accountReq = new AccountRequest(BigDecimal.valueOf(500));
        MvcResult accountResult = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountReq)))
                .andReturn();

        accountNumber = objectMapper.readTree(accountResult.getResponse().getContentAsString())
                .get("accountNumber").asText();
    }

    @Test
    void deposit_Success() throws Exception {
        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(500), null);

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500));
    }

    @Test
    void deposit_Fails_WhenNoToken() throws Exception {
        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(500), null);

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void withdraw_Success() throws Exception {
        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(200), null);

        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void withdraw_Fails_WhenInsufficientFunds() throws Exception {
        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(99999), null);

        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void transfer_Success() throws Exception {
        RegisterRequest register2 = new RegisterRequest("user2", "user2@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)));

        LoginRequest login2 = new LoginRequest("user2@test.com", "password123");
        MvcResult login2Result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login2)))
                .andReturn();
        String token2 = objectMapper.readTree(login2Result.getResponse().getContentAsString())
                .get("token").asText();

        AccountRequest accountReq2 = new AccountRequest(BigDecimal.valueOf(500));
        MvcResult account2Result = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountReq2)))
                .andReturn();
        String accountNumber2 = objectMapper.readTree(account2Result.getResponse().getContentAsString())
                .get("accountNumber").asText();

        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(200), accountNumber2);

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void transfer_Fails_WhenSameAccount() throws Exception {
        TransactionRequest req = new TransactionRequest(accountNumber, BigDecimal.valueOf(200), accountNumber);

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void history_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/history/" + accountNumber)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void history_Fails_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/transactions/history/" + accountNumber))
                .andExpect(status().isUnauthorized());
    }
}