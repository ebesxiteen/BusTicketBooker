package com.example.ticketbooker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Util.Enum.UserStatus;
import com.example.ticketbooker.Util.Mapper.UserMapper;

@Controller
@RequestMapping("/admin/users") // Tất cả gom về đường dẫn này
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Hiển thị danh sách User (Thay thế cho danh sách Account cũ)
    @GetMapping
    public String allUsers(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "keyword", required = false) String keyword) {
        
        // Nếu có từ khóa tìm kiếm
        if (keyword != null && !keyword.isEmpty()) {
            UserResponse searchResult = userService.getAllUserByName(keyword);
            model.addAttribute("users", searchResult.getListUsers());
            model.addAttribute("totalPages", 1); // Tìm kiếm tạm thời để 1 trang
        } else {
            // Phân trang bình thường
            Pageable pageable = PageRequest.of(page, size);
            Page<UserDTO> userDTOPage = this.userService.getAllUsers(pageable);
            
            model.addAttribute("users", userDTOPage.getContent());
            model.addAttribute("totalPages", userDTOPage.getTotalPages());
        }

        model.addAttribute("createUserForm", new AddUserRequest()); // Form thêm mới
        model.addAttribute("currentPage", page);
        
        return "View/Admin/Users/ListUsers";
    }

    // 2. Xem chi tiết / Form sửa (Gộp chung)
    @GetMapping("/details/{id}")
    public String userDetails(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        UserResponse response = userService.getUserById(id);
        
        if (response.getUsersCount() == 1) {
            UserDTO userDTO = response.getListUsers().get(0);
            
            // Chuyển đổi DTO sang Request để đổ dữ liệu vào form
            UpdateUserRequest updateUserRequest = UserMapper.toUpdateDTO(userDTO);
            
            model.addAttribute("updateUserForm", updateUserRequest);
            return "View/Admin/Users/UserDetails";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/admin/users";
        }
    }

    // 3. Xử lý Thêm mới (Bao gồm cả Email, Pass, Role)
    @PostMapping("/create")
    public String createUser(@ModelAttribute("createUserForm") AddUserRequest addUserRequest, RedirectAttributes redirectAttributes) {
        try {
            // Xử lý logic mặc định nếu form không gửi lên
            if(addUserRequest.getStatus() == null) addUserRequest.setStatus(UserStatus.ACTIVE);
            
            boolean result = userService.addUser(addUserRequest);
            
            if (result) {
                redirectAttributes.addFlashAttribute("successMessage", "Tạo người dùng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("failedMessage", "Tạo thất bại! Email có thể đã tồn tại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 4. Xử lý Cập nhật (Bao gồm cả Role, Email)
   @PostMapping("/update")
    public String updateUser(@ModelAttribute("updateUserForm") UpdateUserRequest updateUserRequest,
                             @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile, // Thêm dòng này
                             RedirectAttributes redirectAttributes) {
        try {
            // 1. Cập nhật thông tin text trước
            boolean updateInfoResult = userService.updateUser(updateUserRequest);
            
            // 2. Cập nhật Avatar nếu có file gửi lên
            if (avatarFile != null && !avatarFile.isEmpty()) {
                userService.updateAvatar(updateUserRequest.getUserId(), avatarFile);
            }

            if (updateInfoResult) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
            } else {
                redirectAttributes.addFlashAttribute("failedMessage", "Cập nhật thông tin thất bại.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug nếu có
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }
        
        // Redirect về lại trang chi tiết để xem kết quả
        return "redirect:/admin/users/details/" + updateUserRequest.getUserId();
    }
}