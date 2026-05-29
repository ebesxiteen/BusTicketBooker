package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.Enum.Gender;
import com.example.ticketbooker.Util.Enum.UserStatus;
import com.example.ticketbooker.Util.Mapper.UserMapper;

class UserMapperTest {

    @Test
    void fromAddSetsDefaultsForLocalUser() {
        AddUserRequest request = AddUserRequest.builder()
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .phone("0912345678")
                .password("secret")
                .build();

        Users user = UserMapper.fromAdd(request);

        assertEquals("Nguyen Van A", user.getFullName());
        assertEquals("a@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
        assertEquals("LOCAL", user.getProvider());
        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
        assertTrue(user.isEnabled());
    }

    @Test
    void toDtoAndToUpdateDtoMapUserFields() {
        byte[] photo = new byte[] {1, 2, 3};
        Users user = Users.builder()
                .id(9)
                .fullName("Tran Van B")
                .phone("0987654321")
                .address("Da Nang")
                .dateOfBirth(LocalDate.of(1990, 1, 2))
                .gender(Gender.MALE)
                .profilePhoto(photo)
                .userStatus(UserStatus.INACTIVE)
                .email("b@example.com")
                .role("ADMIN")
                .provider("LOCAL")
                .enabled(true)
                .build();

        UserDTO dto = UserMapper.toDTO(user);
        UpdateUserRequest update = UserMapper.toUpdateDTO(dto);

        assertEquals(9, dto.getUserId());
        assertEquals("Tran Van B", update.getFullName());
        assertEquals("b@example.com", update.getEmail());
        assertEquals("ADMIN", update.getRole());
        assertEquals(UserStatus.INACTIVE, update.getStatus());
    }

    @Test
    void nullAndEmptyInputsReturnEmptySafeResults() {
        assertNull(UserMapper.toDTO(null));
        assertNull(UserMapper.toUpdateDTO(null));

        UserResponse response = UserMapper.toResponseDTO(List.of());

        assertEquals(0, response.getUsersCount());
        assertTrue(response.getListUsers().isEmpty());
    }
}
