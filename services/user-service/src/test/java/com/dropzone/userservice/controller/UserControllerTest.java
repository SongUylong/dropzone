package com.dropzone.userservice.controller;

import com.dropzone.userservice.dto.UserDto;
import com.dropzone.userservice.model.UserStatus;
import com.dropzone.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getUserById_ReturnsUser() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .name("John Smith")
                .email("john@example.com")
                .status(UserStatus.ACTIVE)
                .preferences("{\"theme\":\"dark\"}")
                .purchaseHistoryRef("purchase-history-ref-1001")
                .build();

        given(userService.getUserById(1L)).willReturn(user);

        mockMvc.perform(get("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
