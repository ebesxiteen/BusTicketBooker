package com.example.ticketbooker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ticketbooker.DTO.Driver.AddDriverDTO;
import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Entity.Driver;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Util.Mapper.DriverMapper;

@Controller
@RequestMapping("/admin/drivers")
public class DriverController {
    @Autowired
    private DriverService driverService;
@GetMapping
    public String driverListPage(
            Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", defaultValue = "ALL") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("driverId").descending());
        
        // Gọi hàm search mới (Bạn cần thêm hàm này vào Interface DriverService)
        // Nếu chưa thêm vào Interface thì dùng tạm findAll, nhưng tốt nhất nên thêm vào.
        // Giả sử đã thêm searchDrivers vào Interface:
        Page<DriverDTO> driverPage = ((com.example.ticketbooker.Service.ServiceImp.DriverServiceImp)driverService)
                                      .searchDrivers(keyword, status, pageable);

        model.addAttribute("drivers", driverPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", driverPage.getTotalPages());
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);

        return "View/Admin/Drivers/DriverManagement";
    }
    @GetMapping("/add")
    public String showAddDriver(Model model){
        model.addAttribute("driverDTO", new DriverDTO());
        return "View/Admin/Drivers/DriverDetails";
    }

   @PostMapping("/add")
    public String createDriver(@ModelAttribute("driverDTO") AddDriverDTO dto, RedirectAttributes redirectAttributes) {
        try {
            driverService.addDriver(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài xế thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Nếu lỗi, nên giữ lại dữ liệu form (cần logic phức tạp hơn chút, ở đây redirect tạm)
            return "redirect:/admin/drivers/add"; 
        }
        return "redirect:/admin/drivers";
    }
// Form Cập nhật
    @GetMapping("/{id}")
    public String showUpdateDriver(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = driverService.getDriver(id);
        if(driver != null){
            model.addAttribute("driverDTO", DriverMapper.toDTO(driver));
            // QUAN TRỌNG: Trả về đúng tên file HTML hiện có
            return "View/Admin/Drivers/DriverDetails";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài xế ID: " + id);
            return "redirect:/admin/drivers";
        }
    }

    // XỬ LÝ CẬP NHẬT
    @PostMapping("/update")
    public String updateDriver(@ModelAttribute("driverDTO") UpdateDriverDTO dto, RedirectAttributes redirectAttributes) {
        try {
            driverService.updateDriver(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài xế thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/drivers/" + dto.getDriverId();
        }
        return "redirect:/admin/drivers";
    }
}
