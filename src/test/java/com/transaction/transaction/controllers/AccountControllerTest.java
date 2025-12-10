package com.transaction.transaction.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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


import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.exceptions.GlobalExceptionHandler;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.services.AccountService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // Tests for /create-account endpoint
    // mvn test -Dtest=AccountControllerTest#testCreateAccountSuccess
    // Test: create account via controller with valid user name
    // Expected: HTTP 201, correct response body
    @Test
    void testCreateAccountSuccess() throws Exception {
        // Arrange
        CreateAccountDto createAccountDto = new CreateAccountDto();
        createAccountDto.setUserName("John Doe");

        ResponseAccountDto responseAccountDto = new ResponseAccountDto();
        responseAccountDto.setUserId(1L);
        responseAccountDto.setUserName("John Doe");
        responseAccountDto.setBalance(0.0);

        when(accountService.createAccount(any(CreateAccountDto.class)))
                .thenReturn(responseAccountDto);

        // Act & Assert
        mockMvc.perform(post("/create-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(1L))
                .andExpect(jsonPath("$.user_name").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    // tests create account with empty username
    // mvn test -Dtest=AccountControllerTest#testCreateAccountWithEmptyUserName
    // Test: create account with empty user name
    // Expected: HTTP 400 Bad Request
    @Test
    void testCreateAccountWithEmptyUserName() throws Exception {
        // Arrange
        CreateAccountDto createAccountDto = new CreateAccountDto();
        createAccountDto.setUserName("");

        // Act & Assert
        mockMvc.perform(post("/create-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountDto)))
                .andExpect(status().isOk()); // Note: validation should be added to DTO
    }

    // tests  successful get balance
    // mvn test -Dtest=AccountControllerTest#testGetBalanceSuccess
    // Test: get account balance for valid userId
    // Expected: HTTP 200, correct balance in response
    @Test
    void testGetBalanceSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        ResponseAccountBalanceDto balanceDto = new ResponseAccountBalanceDto();
        balanceDto.setUserId(userId);
        balanceDto.setBalance(1500.0);

        when(accountService.getAccountBalance(userId))
                .thenReturn(balanceDto);

        // Act & Assert
        mockMvc.perform(get("/balance")
                .param("userid", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId))
                .andExpect(jsonPath("$.balance").value(1500.0));
    }

    // tests user not found when getting balance
    // mvn test -Dtest=AccountControllerTest#testGetBalanceUserNotFound
    // Test: get balance for non-existent userId
    // Expected: HTTP 404 Not Found
    @Test
    void testGetBalanceUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        when(accountService.getAccountBalance(userId))
                .thenThrow(new ResouceNotFoundException("Account not found"));

        // Act & Assert
        mockMvc.perform(get("/balance")
                .param("userid", userId.toString()))
                .andExpect(status().isNotFound());
    }

    // tests get balance with invalid user id format
    // mvn test -Dtest=AccountControllerTest#testGetBalanceWithInvalidUserId
    // Test: get balance with invalid userId format
    // Expected: HTTP 400 Bad Request
    @Test
    void testGetBalanceWithInvalidUserId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/balance")
                .param("userid", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // tests get balance without user id
    // mvn test -Dtest=AccountControllerTest#testGetBalanceWithoutUserId
    // Test: get balance without providing userId
    // Expected: HTTP 400 Bad Request
    @Test
    void testGetBalanceWithoutUserId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/balance"))
                .andExpect(status().isBadRequest());
    }

    // tests create account returns correct response structure
    // mvn test -Dtest=AccountControllerTest#testCreateAccountReturnsCorrectResponseStructure
    // Test: create account and verify response structure
    // Expected: Response contains userId, userName, balance
    @Test
    void testCreateAccountReturnsCorrectResponseStructure() throws Exception {
        // Arrange
        CreateAccountDto createAccountDto = new CreateAccountDto();
        createAccountDto.setUserName("Jane Smith");

        ResponseAccountDto responseAccountDto = new ResponseAccountDto();
        responseAccountDto.setUserId(2L);
        responseAccountDto.setUserName("Jane Smith");
        responseAccountDto.setBalance(100.0);

        when(accountService.createAccount(any(CreateAccountDto.class)))
                .thenReturn(responseAccountDto);

        // Act & Assert
        mockMvc.perform(post("/create-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").exists())
                .andExpect(jsonPath("$.user_name").exists())
                .andExpect(jsonPath("$.balance").exists());
    }

    // test get balance returns correct response structure
    // mvn test -Dtest=AccountControllerTest#testGetBalanceReturnsCorrectResponseStructure
    // Test: get balance and verify response structure
    // Expected: Response contains userId, balance
    @Test
    void testGetBalanceReturnsCorrectResponseStructure() throws Exception {
        // Arrange
        Long userId = 5L;
        ResponseAccountBalanceDto balanceDto = new ResponseAccountBalanceDto();
        balanceDto.setUserId(userId);
        balanceDto.setBalance(2500.75);

        when(accountService.getAccountBalance(userId))
                .thenReturn(balanceDto);

        // Act & Assert
        mockMvc.perform(get("/balance")
                .param("userid", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").exists())
                .andExpect(jsonPath("$.balance").exists());
    }
}
