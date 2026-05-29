package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserIdRequest;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.ServiceImp.UserServiceImp;
import com.example.ticketbooker.Util.Enum.Gender;
import com.example.ticketbooker.Util.Enum.UserStatus;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepo usersRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImp userServiceImp;

    @Test
    void processOAuthPostLoginCreatesNewUserWhenEmailDoesNotExist() {
        when(usersRepo.findByEmail("a@example.com")).thenReturn(null);
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        userServiceImp.processOAuthPostLogin("a@example.com", "Nguyen Van A");

        verify(usersRepo).save(userCaptor.capture());
        Users saved = userCaptor.getValue();
        assertEquals("a@example.com", saved.getEmail());
        assertEquals("Nguyen Van A", saved.getFullName());
        assertEquals("GOOGLE", saved.getProvider());
        assertEquals("USER", saved.getRole());
        assertTrue(saved.isEnabled());
    }

    @Test
    void addUserReturnsFalseWhenEmailAlreadyExists() {
        AddUserRequest request = AddUserRequest.builder().email("a@example.com").build();
        when(usersRepo.findByEmail("a@example.com")).thenReturn(new Users());

        assertFalse(userServiceImp.addUser(request));
        verify(usersRepo, never()).save(any());
    }

    @Test
    void addUserEncodesPasswordAndSaves() {
        AddUserRequest request = AddUserRequest.builder()
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .password("secret")
                .build();
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        assertTrue(userServiceImp.addUser(request));

        verify(usersRepo).save(userCaptor.capture());
        assertEquals("encoded", userCaptor.getValue().getPassword());
        assertEquals("LOCAL", userCaptor.getValue().getProvider());
    }

    @Test
    void addUserGetIdReturnsSavedId() {
        AddUserRequest request = AddUserRequest.builder().email("a@example.com").password("secret").build();
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(usersRepo.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setId(99);
            return user;
        });

        assertEquals(99, userServiceImp.addUserGetId(request));
    }

    @Test
    void updateUserOnlyMutatesProvidedFields() {
        Users existing = Users.builder()
                .id(9)
                .fullName("Old")
                .email("old@example.com")
                .phone("090")
                .userStatus(UserStatus.ACTIVE)
                .build();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .userId(9)
                .fullName("New")
                .gender(Gender.FEMALE)
                .status(UserStatus.INACTIVE)
                .build();
        when(usersRepo.findById(9)).thenReturn(Optional.of(existing));

        assertTrue(userServiceImp.updateUser(request));

        assertEquals("New", existing.getFullName());
        assertEquals("old@example.com", existing.getEmail());
        assertEquals(Gender.FEMALE, existing.getGender());
        assertEquals(UserStatus.INACTIVE, existing.getUserStatus());
        verify(usersRepo).save(existing);
    }

    @Test
    void deleteUserReturnsFalseWhenUserDoesNotExist() {
        when(usersRepo.existsById(9)).thenReturn(false);

        assertFalse(userServiceImp.deleteUser(new UserIdRequest(9)));
        verify(usersRepo, never()).deleteById(any());
    }

    @Test
    void sortUserByNameSortsCaseInsensitive() {
        Users b = Users.builder().id(1).fullName("beta").userStatus(UserStatus.ACTIVE).build();
        Users a = Users.builder().id(2).fullName("Alpha").userStatus(UserStatus.ACTIVE).build();
        UserResponse response = userServiceImp.getAllUsers();
        response.setListUsers(new ArrayList<>());
        response.getListUsers().add(com.example.ticketbooker.Util.Mapper.UserMapper.toDTO(b));
        response.getListUsers().add(com.example.ticketbooker.Util.Mapper.UserMapper.toDTO(a));

        UserResponse sorted = userServiceImp.sortUserByName(response);

        assertEquals("Alpha", sorted.getListUsers().get(0).getFullName());
    }

    @Test
    void saveEncodesPasswordAndForcesUserRole() {
        Users user = Users.builder().password("secret").role("ADMIN").build();
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(usersRepo.save(user)).thenReturn(user);

        Users result = userServiceImp.save(user);

        assertEquals("encoded", result.getPassword());
        assertEquals("USER", result.getRole());
    }

    @Test
    void mapToUpdateUserRequestMapsEntityFields() {
        Users user = Users.builder()
                .id(9)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .phone("0912345678")
                .address("Ha Noi")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .userStatus(UserStatus.ACTIVE)
                .role("USER")
                .build();

        UpdateUserRequest dto = userServiceImp.mapToUpdateUserRequest(user);

        assertEquals(9, dto.getUserId());
        assertEquals("a@example.com", dto.getEmail());
        assertEquals(Gender.MALE, dto.getGender());
    }

    @Test
    void updateAvatarStoresMultipartBytes() {
        Users user = Users.builder().id(9).build();
        byte[] bytes = new byte[] {1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.png", "image/png", bytes);
        when(usersRepo.findById(9)).thenReturn(Optional.of(user));

        userServiceImp.updateAvatar(9, file);

        assertArrayEquals(bytes, user.getProfilePhoto());
        verify(usersRepo).save(user);
    }

    @Test
    void addUserGetIdReturnsNullOnDuplicateEmail() {
        AddUserRequest request = AddUserRequest.builder().email("a@example.com").build();
        when(usersRepo.findByEmail("a@example.com")).thenReturn(new Users());

        assertNull(userServiceImp.addUserGetId(request));
    }
}
