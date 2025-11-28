package com.example.ticketbooker.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserIdRequest;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.Enum.Gender;

public interface UserService {
    void processOAuthPostLogin(String email, String name);
    boolean addUser(AddUserRequest dto);
    Integer addUserGetId(AddUserRequest dto);
    boolean updateUser(UpdateUserRequest dto);
    boolean deleteUser(UserIdRequest dto);
    
    UserResponse getAllUsers();
    Page<UserDTO> getAllUsers(Pageable pageable);
    
    UserResponse getUserById(int userId);
    UserResponse getAllUserByName(String username);
    UserResponse getAllUsersByGender(Gender gender);
    UserResponse getAllUserByAddress(String address);
    UserResponse sortUserByName(UserResponse users);
    Users save(Users user);
    UpdateUserRequest mapToUpdateUserRequest(Users user);

}