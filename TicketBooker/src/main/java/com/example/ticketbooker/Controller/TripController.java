package com.example.ticketbooker.Controller;

import java.util.List;

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

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Routes.RouteDTO;
import com.example.ticketbooker.DTO.Trips.AddTripDTO;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.DTO.Trips.TripDTO;
import com.example.ticketbooker.DTO.Trips.UpdateTripDTO;
import com.example.ticketbooker.Service.BusService;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Service.RouteService;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Util.Enum.TripStatus;
import com.example.ticketbooker.Util.Mapper.TripMapper;


@Controller
@RequestMapping("/admin/trips")
public class TripController {
    @Autowired
    private TripService tripService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private DriverService driverService;

    @Autowired
    private BusService busService;

@GetMapping()
public String listTrips(
    Model model,
    @RequestParam(name = "status", required = false) String status,
    @RequestParam(name = "page", defaultValue = "0") int page,
    @RequestParam(name = "size", defaultValue = "10") int size) {

    // 1. THÊM SẮP XẾP: Theo ID giảm dần
    // Giả định trường ID trong Entity là "id"
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending()); 
    
    // 2. GỌI SERVICE MỚI (Lọc + Phân trang)
    Page<TripDTO> tripPage = tripService.getAllTrips(status, pageable);

    // Log danh sách TripDTO ra console
    System.out.println("Danh sách TripDTO:");
    tripPage.getContent().forEach(System.out::println);

    List<BusDTO> buses = busService.getAllBuses();
    model.addAttribute("buses", buses);

    // 3. GÁN ĐỐI TƯỢNG PAGE VÀO MODEL
    model.addAttribute("tripsPage", tripPage);
    
    // 4. GÁN TRẠNG THÁI HIỆN TẠI VÀO MODEL ĐỂ VIEW GIỮ LẠI (GIỮ LẠI BỘ LỌC)
    model.addAttribute("currentStatus", status != null ? status : "ALL");

    model.addAttribute("currentPage", page);
    model.addAttribute("createTripForm", new AddTripDTO());
    model.addAttribute("updateTripForm", new UpdateTripDTO());
    
    return "View/Admin/Trips/AllTrips"; 
}


    @PostMapping("/create")
    public String createTrip(@ModelAttribute("createTripForm") AddTripDTO addTripDTO, Model model) {
        try {
            addTripDTO.setTripStatus(TripStatus.SCHEDULED);
            System.out.println("Creating trip: " + addTripDTO);

            boolean result = tripService.addTrip(addTripDTO);
            if (result) {
                model.addAttribute("successMessage", "Successfully created");
            } else {
                model.addAttribute("failedMessage", "Trip creation has failed");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding trip: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/trips";
    }
// THÊM CHUYẾN – HIỂN THỊ FORM
@GetMapping("/add")
public String showAddTripForm(Model model) {
    // 1. Khởi tạo DTO
    AddTripDTO dto = new AddTripDTO();
    model.addAttribute("addTripForm", dto);

    // 2. Lấy dữ liệu cho combobox và Khai báo biến cục bộ
    // ĐẢM BẢO CÁC DTO VÀ "List" ĐƯỢC IMPORT CHÍNH XÁC
    List<RouteDTO> routes = routeService.getAllRoutes();
    List<DriverDTO> drivers = driverService.getAllDrivers();
    List<BusDTO> buses = busService.getAllBuses();

    // Log (Tùy chọn, để kiểm tra dữ liệu)
    System.out.println("ROUTES COUNT: " + (routes != null ? routes.size() : "NULL"));
    System.out.println("DRIVERS COUNT: " + (drivers != null ? drivers.size() : "NULL"));
    System.out.println("BUSES COUNT: " + (buses != null ? buses.size() : "NULL"));

    // 3. Thêm vào Model
    model.addAttribute("routes", routes);
    model.addAttribute("drivers", drivers);
    model.addAttribute("buses", buses);

    return "View/Admin/Trips/AddTrip";
}

// THÊM CHUYẾN – XỬ LÝ SUBMIT
@PostMapping("/add")
public String addTrip(@ModelAttribute("addTripForm") AddTripDTO addTripDTO, Model model, RedirectAttributes redirectAttributes) {
    try {
        // Nhận kết quả boolean từ Service
        boolean result = tripService.addTrip(addTripDTO); 
        
        if (result) {
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chuyến xe thành công!");
            return "redirect:/admin/trips";
        } else {
            // Trường hợp hiếm: logic service không ném exception nhưng không lưu được
            throw new RuntimeException("Lưu chuyến xe không thành công (lỗi không xác định).");
        }
    } catch (RuntimeException e) {
        // Bắt lỗi trùng lịch (từ Service ném ra) hoặc các lỗi khác
        model.addAttribute("errorMessage", e.getMessage());
        
        // Tải lại dữ liệu cho các select box (QUAN TRỌNG ĐỂ FORM RENDER LẠI)
        model.addAttribute("addTripForm", addTripDTO);
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("buses", busService.getAllBuses());
        
        return "View/Admin/Trips/AddTrip";
    }
}


@GetMapping("/admin/trips/{id}")
public String editTrip(@PathVariable int id, Model model) {
    ResponseTripDTO trip = tripService.getTripById(id);
    model.addAttribute("updateTripForm", trip);
    return "TripDetail";
}

@PostMapping("/update")
    public String update(@ModelAttribute("trip") UpdateTripDTO tripDTO, 
                               RedirectAttributes redirectAttributes) { // <--- Thêm tham số này
    try {
        // Gọi service
        if (tripDTO.getTripId() != null) {
            tripService.updateTrip(tripDTO); // Hàm này sẽ ném lỗi nếu validation sai
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chuyến xe thành công!");
        } else {
            // Logic thêm mới...
        }
        
        return "redirect:/admin/trips"; // Redirect về trang danh sách

    } catch (RuntimeException e) {
        // QUAN TRỌNG: Bắt lỗi từ Service và gửi ra View
        e.printStackTrace(); // In lỗi ra console để debug xem đúng lỗi mình ném không
        
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        
        return "redirect:/admin/trips/details/" + tripDTO.getTripId(); 
    }
}


@GetMapping("/details/{id}")
public String tripDetails(@PathVariable int id, Model model) {
    UpdateTripDTO updateTripDTO = new UpdateTripDTO();
    
    // Giả định getTripByIds trả về thông tin chuyến xe cần sửa
    ResponseTripDTO tripResponse = tripService.getTripByIds(id);
    
    if(tripResponse.getTripsCount() == 1){
        // Mapping Entity sang DTO cập nhật
        updateTripDTO = TripMapper.toUpdateDTO(tripResponse.getListTrips().get(0));
    }
    
    // 1. LẤY DANH SÁCH DỮ LIỆU CẦN THIẾT
    List<RouteDTO> routes = routeService.getAllRoutes(); 
    List<BusDTO> buses = busService.getAllBuses();
    List<DriverDTO> drivers = driverService.getAllDrivers();

    // 2. GÁN VÀO MODEL
    model.addAttribute("routes", routes); // <-- Thêm Routes
    model.addAttribute("buses", buses);   // <-- Thêm Buses
    model.addAttribute("drivers", drivers); // <-- Thêm Drivers
    
    model.addAttribute("updateTripForm", updateTripDTO);
    
    return "View/Admin/Trips/TripDetail"; // Đảm bảo đúng đường dẫn View
}
}
