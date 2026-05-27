package com.example.ticketbooker.Util.Mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.Enum.UserStatus;

public class UserMapper {

    // ========================================================================
    // 1. TỪ ENTITY (DB) -> DTO (TRẢ VỀ CHO FRONTEND)
    // ========================================================================
    public static UserDTO toDTO(Users entity) {
        if (entity == null) return null;

        return UserDTO.builder()
                .userId(entity.getId())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .profilePhoto(entity.getProfilePhoto())
                .status(entity.getUserStatus())
                .email(entity.getEmail())
                .role(entity.getRole())
                .provider(entity.getProvider())
                .enabled(entity.isEnabled())
                .build();
    }

    // ========================================================================
    // 2. TỪ ADD REQUEST (ĐĂNG KÝ) -> ENTITY (LƯU DB)
    // ========================================================================
    public static Users fromAdd(AddUserRequest dto) {
        Users user = new Users();
        
        // Map thông tin cơ bản
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        
        // Map thông tin đăng nhập (QUAN TRỌNG)
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // mã hóa ở Service
        user.setRole(dto.getRole() != null ? dto.getRole() : "USER"); // Nếu không gửi role, mặc định là USER
        
        // Set giá trị mặc định cho User mới
        user.setProvider("LOCAL"); // Đăng ký qua form thì luôn là LOCAL
        user.setEnabled(true);     // Mặc định kích hoạt tài khoản
        
        // Xử lý status (tránh null)
        user.setUserStatus(dto.getStatus() != null ? dto.getStatus() : UserStatus.ACTIVE);

        return user;
    }

    // ========================================================================
    // 3. CHUYỂN DANH SÁCH ENTITY -> USER RESPONSE (CHỨA LIST DTO)
    // ========================================================================
    public static UserResponse toResponseDTO(List<Users> listEntities) {
        UserResponse response = new UserResponse();
        
        if (listEntities != null && !listEntities.isEmpty()) {
            // Dùng Stream để chuyển đổi từng Users -> UserDTO
            List<UserDTO> listDTOs = listEntities.stream()
                    .map(UserMapper::toDTO)
                    .collect(Collectors.toList());

            response.setUsersCount(listDTOs.size());
            response.setListUsers(listDTOs);
        } else {
            // Trả về danh sách rỗng nếu không có dữ liệu
            response.setUsersCount(0);
            response.setListUsers(List.of());
        }
        
        return response;
    }

    // ========================================================================
    // 4. TỪ DTO -> UPDATE REQUEST (ĐỂ ĐỔ DỮ LIỆU LÊN FORM SỬA)
    // ========================================================================
    public static UpdateUserRequest toUpdateDTO(UserDTO dto) {
        if (dto == null) return null;

        return UpdateUserRequest.builder()
                .userId(dto.getUserId())
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .profilePhoto(dto.getProfilePhoto())
                .status(dto.getStatus())
                
                // --- MAP CÁC TRƯỜNG QUẢN TRỊ ---
                .email(dto.getEmail())
                .role(dto.getRole())
                // --------------------------------
                .build();
    }
}