package com.vaultflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String walletId;
    private String cardId;

    @BeforeAll
    void setUp() throws Exception {
        var registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "TxCorp", "taxId": "T-TAX-001", "email": "tx@corp.com", "password": "pass123"}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        token = objectMapper.readTree(registerResult.getResponse().getContentAsString())
            .get("token").asText();

        var walletsResult = mockMvc.perform(get("/api/v1/wallets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        walletId = objectMapper.readTree(walletsResult.getResponse().getContentAsString())
            .get(0).get("id").asText();

        var cardResult = mockMvc.perform(post("/api/v1/wallets/" + walletId + "/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"holderName": "Payer", "limitAmount": 5000.0}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        cardId = objectMapper.readTree(cardResult.getResponse().getContentAsString())
            .get("id").asText();
    }

    @Test
    void pay_shouldCompleteTransaction() throws Exception {
        String secondWalletId = createWallet();

        String payBody = String.format("""
            {"fromCardId": "%s", "toWalletId": "%s", "amount": 100.0, "description": "Test payment"}
            """, cardId, secondWalletId);

        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    void pay_shouldFailWhenInsufficientFunds() throws Exception {
        String secondWalletId = createWallet();

        String payBody = String.format("""
            {"fromCardId": "%s", "toWalletId": "%s", "amount": 999999.0}
            """, cardId, secondWalletId);

        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payBody))
            .andExpect(status().isConflict());
    }

    @Test
    void listTransactions_shouldReturnHistory() throws Exception {
        String secondWalletId = createWallet();

        String payBody = String.format("""
            {"fromCardId": "%s", "toWalletId": "%s", "amount": 50.0}
            """, cardId, secondWalletId);

        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payBody))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    private String createWallet() throws Exception {
        var result = mockMvc.perform(post("/api/v1/wallets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currency": "USD"}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asText();
    }
}
