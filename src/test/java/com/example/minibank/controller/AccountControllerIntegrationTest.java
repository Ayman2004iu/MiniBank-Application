package com.example.minibank.controller;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.LoginRequest;
import com.example.minibank.dto.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String otherToken;

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

        RegisterRequest otherRegisterReq = new RegisterRequest("rana", "rana@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherRegisterReq)));

        LoginRequest otherLoginReq = new LoginRequest("rana@test.com", "password123");
        MvcResult otherLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLoginReq)))
                .andReturn();

        otherToken = objectMapper.readTree(otherLoginResult.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void createAccount_Success() throws Exception {
        AccountRequest req = new AccountRequest(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    void getMyAccounts_Success() throws Exception {
        AccountRequest req = new AccountRequest(BigDecimal.valueOf(500));
        mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AccountRequest(BigDecimal.valueOf(200)))));

        mockMvc.perform(get("/api/accounts/my-accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMyAccounts_ReturnsEmpty_WhenNoAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/my-accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMyAccounts_Fails_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/accounts/my-accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAccount_Success_AsOwner() throws Exception {
        AccountRequest req = new AccountRequest(BigDecimal.valueOf(500));
        MvcResult createResult = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        String accountNumber = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("accountNumber").asText();

        mockMvc.perform(get("/api/accounts/" + accountNumber)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    void getAccount_Fails_WhenNotOwner() throws Exception {
        AccountRequest req = new AccountRequest(BigDecimal.valueOf(500));
        MvcResult createResult = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        String accountNumber = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("accountNumber").asText();

        mockMvc.perform(get("/api/accounts/" + accountNumber)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAccount_Fails_WhenAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/MB00000000000000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccount_Fails_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/accounts/MB00000000000000"))
                .andExpect(status().isUnauthorized());
    }
}