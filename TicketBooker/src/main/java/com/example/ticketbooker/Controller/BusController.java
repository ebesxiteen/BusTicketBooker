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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Service.RouteService;

@Controller
@RequestMapping("/admin/buses")
public class BusController {

    @Autowired
    private BusService busService;

    @Autowired
    private RouteService routeService;

    //Hiển thị danh sách buses
   @GetMapping
    public String listBuses(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", defaultValue = "ALL") String status,
            @RequestParam(name = "type", defaultValue = "ALL") String type) {

        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi hàm search mới (thay thế logic cũ)
        Page<BusDTO> busPage = busService.searchBuses(keyword, status, type, pageable);

        model.addAttribute("buses", busPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", busPage.getTotalPages());
        
        // Gửi lại giá trị lọc để giữ trạng thái trên giao diện
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentType", type);

        return "View/Admin/Bus/Bus";
    }

    //Hiển thị form thêm bus
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("busDTO", new BusDTO());
        model.addAttribute("routes", routeService.findAllRoutes().getList()); // Lấy danh sách tuyến
        return "View/Admin/Bus/BusForm";
    }

    // Xử lý thêm bus
    @PostMapping("/add")
    public String addBus(@ModelAttribute("busDTO") BusDTO busDTO, RedirectAttributes redirectAttributes) {
        busService.createBus(busDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm xe thành công!");
        return "redirect:/admin/buses";
    }

    // Hiển thị form sửa bus
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        BusDTO busDTO = busService.getBusById(id);
        if (busDTO != null) {
            model.addAttribute("busDTO", busDTO);
            model.addAttribute("routes", routeService.findAllRoutes().getList()); // Lấy danh sách tuyến
            return "View/Admin/Bus/BusForm";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy xe với ID: " + id);
            return "redirect:/admin/buses";
        }
    }

    // Xử lý sửa bus
// Xử lý sửa bus
    @PostMapping("/update")
    public String updateBus(@ModelAttribute("busDTO") BusDTO busDTO, RedirectAttributes redirectAttributes) {
        try {
            // Gọi service update
            // Nếu có lỗi logic (ghế, vé...), Service sẽ ném RuntimeException
            busService.updateBus(busDTO);
            
            // Nếu chạy đến đây nghĩa là thành công
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật xe thành công!");
            
        } catch (RuntimeException e) {
            // BẮT LỖI Ở ĐÂY: Lấy message từ Service gửi ra View
            e.printStackTrace(); // In lỗi ra console để debug
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
        } catch (Exception e) {
            // Các lỗi hệ thống khác
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }
        
        return "redirect:/admin/buses";
    }

    @GetMapping("/search")
    public String searchBuses(
            Model model,
            @RequestParam(name = "licensePlate", required = false) String licensePlate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        // If licensePlate is not null or empty, perform search, otherwise get all buses
        Page<BusDTO> busPage = (licensePlate != null && !licensePlate.isEmpty()) ?
                busService.getBusesByLicensePlateContaining(licensePlate, pageable) :
                busService.getAllBuses(pageable);


        model.addAttribute("buses", busPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", busPage.getTotalPages());
        model.addAttribute("licensePlate", licensePlate); // Add this line to persist the search term in the input field

        return "View/Admin/Bus/Bus";
    }
}
