package com.example.ticketbooker.Service.ServiceImp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserIdRequest;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Util.Enum.Gender;
import com.example.ticketbooker.Util.Enum.UserStatus;
import com.example.ticketbooker.Util.Mapper.UserMapper;

@Service
@Transactional // Đảm bảo tính toàn vẹn dữ liệu
public class UserServiceImp implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImp.class);

    @Autowired
    private UserRepo usersRepo;

    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------
    // 1. LOGIC OAUTH2 (GOOGLE/FACEBOOK)
    // ----------------------------------------------------------------
    @Override
    public void processOAuthPostLogin(String email, String name) {
        // Bước 1: Tìm trong DB xem email này đã có chưa
        Users existUser = usersRepo.findByEmail(email);

        if (existUser == null) {
            // Trường hợp A: Chưa có -> Tạo mới (INSERT)
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setProvider("GOOGLE");
            newUser.setUserStatus(UserStatus.ACTIVE);
            newUser.setEnabled(true);
            
            // Set các giá trị mặc định khác nếu cần
            newUser.setRole("USER"); 

            usersRepo.save(newUser);
            logger.info("Created new OAuth user: " + email);
        } else {
            // Trường hợp B: Đã có -> Cập nhật thông tin (UPDATE)
            existUser.setFullName(name); 
            existUser.setProvider("GOOGLE");
            
            usersRepo.save(existUser);
            logger.info("Updated existing OAuth user: " + email);
        }
    }

    // ----------------------------------------------------------------
    // 2. CÁC HÀM CRUD CƠ BẢN (Đã tối ưu)
    // ----------------------------------------------------------------

    @Override
    public boolean addUser(AddUserRequest dto) {
        try {

            // Kiểm tra email trùng lặp cho đăng ký thường
            if (usersRepo.findByEmail(dto.getEmail()) != null) {
                logger.warn("Add user failed: Email {} already exists", dto.getEmail());
                return false;
            }
            

            Users user = UserMapper.fromAdd(dto);
            // Set mặc định cho user mới
            if (user.getProvider() == null) user.setProvider("LOCAL");
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            logger.info("Adding new user: {}", user);
            this.usersRepo.save(user);
            return true;
        } catch (Exception e) {
            logger.error("Error adding user: ", e);
            return false;
        }
    }

    @Override
    public Integer addUserGetId(AddUserRequest dto) {
        try {
             // Kiểm tra trùng lặp
            if (usersRepo.findByEmail(dto.getEmail()) != null) {
                logger.warn("Add user failed: Email {} already exists", dto.getEmail());
                return null;
            }

            Users user = UserMapper.fromAdd(dto);
            if (user.getProvider() == null) user.setProvider("LOCAL");
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            user = this.usersRepo.save(user);
            return user.getId();
        } catch (Exception e) {
            logger.error("Error adding user and getting ID: ", e);
            return null;
        }
    }

    @Override
    public boolean updateUser(UpdateUserRequest dto) {
        try {
        Users user = usersRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User ID not found"));

        // Chỉ update những field cho phép sửa
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getProfilePhoto() != null) user.setProfilePhoto(dto.getProfilePhoto());

        // Status: nếu form không gửi thì giữ nguyên trạng thái cũ
        if (dto.getStatus() != null) {
            user.setUserStatus(dto.getStatus());
        }

        // Email & role: tùy bà có cho sửa từ form hay không
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getRole() != null) user.setRole(dto.getRole());

        // 👉 TUYỆT ĐỐI KHÔNG ĐỤNG TỚI password Ở ĐÂY

        usersRepo.save(user);
        logger.info("Updated user ID: {}", dto.getUserId());
        return true;
    } catch (Exception e) {
        logger.error("Error updating user: ", e);
        return false;
    }
    }

    @Override
    public boolean deleteUser(UserIdRequest dto) {
        try {
            if (usersRepo.existsById(dto.getUserId())) {
                this.usersRepo.deleteById(dto.getUserId());
                logger.info("Deleted user ID: {}", dto.getUserId());
                return true;
            }
            logger.warn("Delete failed: User ID {} not found", dto.getUserId());
            return false;
        } catch (Exception e) {
            logger.error("Error deleting user: ", e);
            return false;
        }
    }

    // ----------------------------------------------------------------
    // 3. CÁC HÀM GET / SEARCH / SORT
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true) // Tối ưu hiệu năng cho thao tác đọc
    public UserResponse getAllUsers() {
        try {
            return UserMapper.toResponseDTO(this.usersRepo.findAll());
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return new UserResponse();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<Users> userPages = this.usersRepo.findAll(pageable);
        return userPages.map(UserMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(int userId) {
        try {
            return UserMapper.toResponseDTO(this.usersRepo.findAllById(userId));
        } catch (Exception e) {
            logger.error("Error getting user by ID: " + userId, e);
            return new UserResponse();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAllUserByName(String username) {
        try {
            return UserMapper.toResponseDTO(this.usersRepo.findByFullNameLike(username));
        } catch (Exception e) {
            logger.error("Error searching user by name: " + username, e);
            return new UserResponse();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAllUsersByGender(Gender gender) {
        try {
            return UserMapper.toResponseDTO(this.usersRepo.findAllByGender(gender));
        } catch (Exception e) {
            logger.error("Error searching user by gender", e);
            return new UserResponse();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAllUserByAddress(String address) {
        try {
            return UserMapper.toResponseDTO(this.usersRepo.findAllByAddress(address));
        } catch (Exception e){
            logger.error("Error searching user by address: " + address, e);
            return new UserResponse();
        }
    }

    @Override
    public UserResponse sortUserByName(UserResponse users) {
        try {
            if (users != null && users.getListUsers() != null && !users.getListUsers().isEmpty()) {
                users.getListUsers().sort((user1, user2) -> user1.getFullName().compareToIgnoreCase(user2.getFullName()));
            }
        } catch (Exception e) {
            logger.error("Error sorting users", e);
        }
        return users;
    }

    @Override
    public Users save(Users user) {
        // Chỉ mã hóa khi password không phải null (ví dụ: không mã hóa khi cập nhật những field khác)
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            //MÃ HÓA PASSWORD TRƯỚC KHI LƯU
            user.setPassword(passwordEncoder.encode(user.getPassword())); 
        }
        user.setRole("USER"); // Đảm bảo gán Role
        // ... (các logic khác)
        return usersRepo.save(user);
    }
    @Override
public UpdateUserRequest mapToUpdateUserRequest(Users user) {
    UpdateUserRequest dto = new UpdateUserRequest();
    dto.setUserId(user.getId());
    dto.setFullName(user.getFullName());
    dto.setEmail(user.getEmail());
    dto.setPhone(user.getPhone());
    dto.setAddress(user.getAddress());
    dto.setDateOfBirth(user.getDateOfBirth());
    dto.setGender(user.getGender());
    dto.setStatus(user.getUserStatus());
    dto.setRole(user.getRole());
    return dto;
}

@Override
public void updateAvatar(Integer userId, MultipartFile avatarFile) {
    try {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // log nhẹ cho chắc
        System.out.println("Upload avatar size = " + avatarFile.getSize());

        byte[] photoBytes = avatarFile.getBytes();
        user.setProfilePhoto(photoBytes);

        usersRepo.save(user);
    } catch (IOException e) {
        // quấn lại thành RuntimeException cho gọn
        throw new RuntimeException("Lỗi khi xử lý file ảnh", e);
    }
}



    

}