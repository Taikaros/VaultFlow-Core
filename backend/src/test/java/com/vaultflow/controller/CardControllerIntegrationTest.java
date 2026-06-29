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
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String walletId;

    @BeforeAll
    void setUp() throws Exception {
        var registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "CardCorp", "taxId": "C-TAX-001", "email": "card@corp.com", "password": "pass123"}
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
    }

    @Test
    void create_shouldReturn201AndActiveCard() throws Exception {
        String cardBody = """
            {"holderName": "John Doe", "limitAmount": 5000.0}
            """;

        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.holderName").value("John Doe"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.cardNumber").isString());
    }

    @Test
    void list_shouldReturnCards() throws Exception {
        mockMvc.perform(get("/api/v1/cards")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void update_shouldChangeStatus() throws Exception {
        var createResult = mockMvc.perform(post("/api/v1/wallets/" + walletId + "/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"holderName": "Test"}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        String cardId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(patch("/api/v1/cards/" + cardId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "SUSPENDED"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void cancel_shouldReturn204() throws Exception {
        var createResult = mockMvc.perform(post("/api/v1/wallets/" + walletId + "/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"holderName": "CancelMe"}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        String cardId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(delete("/api/v1/cards/" + cardId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }
}
