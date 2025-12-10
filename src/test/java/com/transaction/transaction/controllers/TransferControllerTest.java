package com.transaction.transaction.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import com.transaction.transaction.dto.CreateTransferDto;
import com.transaction.transaction.exceptions.GlobalExceptionHandler;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.services.TransferService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }
    
    // test create transfer success
    @Test
    void testCreateTransferSuccess() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(500.0);

        Long transferId = 100L;

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenReturn(transferId);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transfer_id").value(transferId))
                .andExpect(jsonPath("$.message").value("Transfer successful"))
                .andExpect(jsonPath("$.status").value("success"));
    }

    // test create transfer with null from account id
    @Test
    void testCreateTransferWithNullFromAccountId() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(null);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(500.0);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer with null to account id
    @Test
    void testCreateTransferWithNullToAccountId() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(null);
        createTransferDto.setAmount(500.0);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer with null amount
    @Test
    void testCreateTransferWithNullAmount() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(null);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer from account not found
    @Test
    void testCreateTransferFromAccountNotFound() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(999L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(500.0);

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenThrow(new ResouceNotFoundException("From account not found"));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isNotFound());
    }

    // test create transfer to account not found
    @Test
    void testCreateTransferToAccountNotFound() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(999L);
        createTransferDto.setAmount(500.0);

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenThrow(new ResouceNotFoundException("To account not found"));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isNotFound());
    }

    // test create transfer with insufficient balance
    @Test
    void testCreateTransferInsufficientBalance() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(10000.0);

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenThrow(new IllegalArgumentException("Insufficient balance in the source account"));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer to same account
    @Test
    void testCreateTransferNegativeAmount() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(-100.0);

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenThrow(new IllegalArgumentException("Transfer amount must be positive"));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer to same account
    @Test
    void testCreateTransferToSameAccount() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(1L);
        createTransferDto.setAmount(500.0);

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenThrow(new IllegalArgumentException("Cannot transfer to the same account"));

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isBadRequest());
    }

    // test create transfer with invalid json format
    @Test
    void testCreateTransferWithInvalidJson() throws Exception {
        // Arrange
        String invalidJson = "{\"from_account_id\": \"invalid\", \"to_account_id\": 2, \"amount\": 500}";

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    //test create transfer with empty body 
    @Test
    void testCreateTransferWithEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // test create transfer returns correct response structure
    @Test
    void testCreateTransferReturnsCorrectResponseStructure() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(1000.0);

        Long transferId = 200L;

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenReturn(transferId);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transfer_id").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.transfer_id").value(200L))
                .andExpect(jsonPath("$.message").value("Transfer successful"))
                .andExpect(jsonPath("$.status").value("success"));
    }

    // test create transfer with large amount
    @Test
    void testCreateTransferWithLargeAmount() throws Exception {
        // Arrange
        CreateTransferDto createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L);
        createTransferDto.setToAccountId(2L);
        createTransferDto.setAmount(1000000.0);

        Long transferId = 300L;

        when(transferService.createTransfer(any(CreateTransferDto.class)))
                .thenReturn(transferId);

        // Act & Assert
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transfer_id").value(transferId));
    }
}
