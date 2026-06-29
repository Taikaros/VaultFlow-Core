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
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeAll
    void setUp() throws Exception {
        String registerBody = """
            {"name": "WalletCorp", "taxId": "W-TAX-001", "email": "wallet@corp.com", "password": "pass123"}
            """;

        var result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated())
            .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test
    void list_shouldReturnWallets() throws Exception {
        mockMvc.perform(get("/api/v1/wallets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/wallets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currency": "EUR"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void getById_shouldReturnWallet() throws Exception {
        var listResult = mockMvc.perform(get("/api/v1/wallets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        var walletId = objectMapper.readTree(listResult.getResponse().getContentAsString())
            .get(0).get("id").asText();

        mockMvc.perform(get("/api/v1/wallets/" + walletId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(walletId));
    }

    @Test
    void getById_shouldReturn400WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/non-existent")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest());
    }
}
