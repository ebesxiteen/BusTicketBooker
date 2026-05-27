package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.*;

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
    void toDTOReturnsNullWhenEntityIsNull() {
        assertNull(UserMapper.toDTO(null));
    }

    @Test
    void toDTOMapsAllFieldsFromEntity() {
        Users entity = Users.builder()
                .id(1)
                .fullName("Alice")
                .phone("123456789")
                .address("Wonderland")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.FEMALE)
                .profilePhoto(new byte[] {1, 2, 3})
                .userStatus(UserStatus.ACTIVE)
                .email("alice@example.com")
                .role("ADMIN")
                .provider("GOOGLE")
                .enabled(true)
                .build();

        UserDTO dto = UserMapper.toDTO(entity);

        assertEquals(1, dto.getUserId());
        assertEquals("Alice", dto.getFullName());
        assertEquals("123456789", dto.getPhone());
        assertEquals("Wonderland", dto.getAddress());
        assertEquals(LocalDate.of(2000, 1, 1), dto.getDateOfBirth());
        assertEquals(Gender.FEMALE, dto.getGender());
        assertArrayEquals(new byte[] {1, 2, 3}, dto.getProfilePhoto());
        assertEquals(UserStatus.ACTIVE, dto.getStatus());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
        assertEquals("GOOGLE", dto.getProvider());
        assertTrue(dto.isEnabled());
    }

    @Test
    void fromAddAppliesDefaultsWhenOptionalFieldsMissing() {
        AddUserRequest request = AddUserRequest.builder()
                .fullName("Bob")
                .phone("987654321")
                .email("bob@example.com")
                .password("secret")
                .build();

        Users user = UserMapper.fromAdd(request);

        assertEquals("Bob", user.getFullName());
        assertEquals("987654321", user.getPhone());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals("USER", user.getRole());
        assertEquals("LOCAL", user.getProvider());
        assertTrue(user.isEnabled());
        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
    }

    @Test
    void toResponseDTOReturnsEmptyListWhenInputIsNullOrEmpty() {
        UserResponse fromNull = UserMapper.toResponseDTO(null);
        assertEquals(0, fromNull.getUsersCount());
        assertTrue(fromNull.getListUsers().isEmpty());

        UserResponse fromEmpty = UserMapper.toResponseDTO(List.of());
        assertEquals(0, fromEmpty.getUsersCount());
        assertTrue(fromEmpty.getListUsers().isEmpty());
    }

    @Test
    void toResponseDTOMapsUserListToDTOs() {
        Users first = Users.builder().id(1).fullName("User One").email("one@example.com").build();
        Users second = Users.builder().id(2).fullName("User Two").email("two@example.com").build();

        UserResponse response = UserMapper.toResponseDTO(List.of(first, second));

        assertEquals(2, response.getUsersCount());
        assertEquals(2, response.getListUsers().size());
        assertEquals("User One", response.getListUsers().get(0).getFullName());
        assertEquals("two@example.com", response.getListUsers().get(1).getEmail());
    }

    @Test
    void toUpdateDTOMapsFieldsForEditForms() {
        UserDTO dto = UserDTO.builder()
                .userId(9)
                .fullName("Charlie")
                .phone("5555555")
                .address("Street 9")
                .dateOfBirth(LocalDate.of(1995, 12, 31))
                .gender(Gender.MALE)
                .profilePhoto(new byte[] {9, 9})
                .status(UserStatus.INACTIVE)
                .email("charlie@example.com")
                .role("STAFF")
                .build();

        UpdateUserRequest updateRequest = UserMapper.toUpdateDTO(dto);

        assertEquals(9, updateRequest.getUserId());
        assertEquals("Charlie", updateRequest.getFullName());
        assertEquals("5555555", updateRequest.getPhone());
        assertEquals("Street 9", updateRequest.getAddress());
        assertEquals(LocalDate.of(1995, 12, 31), updateRequest.getDateOfBirth());
        assertEquals(Gender.MALE, updateRequest.getGender());
        assertArrayEquals(new byte[] {9, 9}, updateRequest.getProfilePhoto());
        assertEquals(UserStatus.INACTIVE, updateRequest.getStatus());
        assertEquals("charlie@example.com", updateRequest.getEmail());
        assertEquals("STAFF", updateRequest.getRole());
    }
}
