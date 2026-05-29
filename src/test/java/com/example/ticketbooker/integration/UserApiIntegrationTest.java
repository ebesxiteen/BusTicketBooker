package com.example.ticketbooker.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Enum.UserStatus;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=dummy-test-key",
        "spring.ai.openai.base-url=https://example.com",
        "spring.ai.openai.chat.options.model=llama3-8b-8192",
        "ZALO_APP_ID=demo-app-id",
        "ZALO_KEY1=demo-key-1",
        "ZALO_KEY2=demo-key-2",
        "ZALO_ENDPOINT=https://sandbox.zalopay.vn/v001/tpe/createorder"
})
@AutoConfigureMockMvc(addFilters = false)
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private EmailService emailService;

    @Test
    void deleteUserReturnsBadRequestWhenIdMissing() throws Exception {
        mockMvc.perform(delete("/api/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserReturnsOkWhenServiceDeletes() throws Exception {
        when(userService.deleteUser(any())).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 7}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted successfully"));
    }

    @Test
    void updateUserStatusUpdatesExistingUser() throws Exception {
        UserDTO existing = UserDTO.builder()
                .userId(7)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        UserResponse response = new UserResponse(1, List.of(existing));
        when(userService.getUserById(7)).thenReturn(response);
        when(userService.updateUser(any())).thenReturn(true);

        mockMvc.perform(patch("/api/users/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUser(any());
    }

    @Test
    void existAccountReturnsOkWhenEmailExists() throws Exception {
        when(userRepo.findByEmail("a@example.com")).thenReturn(new Users());

        mockMvc.perform(get("/api/users/exist").param("email", "a@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void confirmResetSendsEmailWhenUserExists() throws Exception {
        Users user = Users.builder().id(7).email("a@example.com").build();
        when(userRepo.findByEmail("a@example.com")).thenReturn(user);
        when(emailService.sendEmail(any(), any(), any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/users/confirm-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@example.com\",\"newPassword\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void resetPasswordEncodesPasswordAndRedirects() throws Exception {
        Users user = Users.builder().id(7).email("a@example.com").build();
        when(userRepo.findById(7)).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/users/reset-password")
                        .param("userId", "7")
                        .param("newPassword", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth"));

        verify(userRepo).save(user);
    }
}
