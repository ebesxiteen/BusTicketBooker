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

    @GetMapping
    public String listTrips(
            Model model,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<TripDTO> tripPage = tripService.getAllTrips(status, pageable);

        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("tripsPage", tripPage);
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
            boolean result = tripService.addTrip(addTripDTO);
            if (result) {
                model.addAttribute("successMessage", "Successfully created");
            } else {
                model.addAttribute("failedMessage", "Trip creation has failed");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding trip: " + e.getMessage());
        }
        return "redirect:/admin/trips";
    }

    @GetMapping("/add")
    public String showAddTripForm(Model model) {
        model.addAttribute("addTripForm", new AddTripDTO());
        addTripFormOptions(model);
        return "View/Admin/Trips/AddTrip";
    }

    @PostMapping("/add")
    public String addTrip(@ModelAttribute("addTripForm") AddTripDTO addTripDTO,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            boolean result = tripService.addTrip(addTripDTO);
            if (result) {
                redirectAttributes.addFlashAttribute("successMessage", "Them chuyen xe thanh cong!");
                return "redirect:/admin/trips";
            }
            throw new RuntimeException("Luu chuyen xe khong thanh cong.");
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("addTripForm", addTripDTO);
            addTripFormOptions(model);
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
                         RedirectAttributes redirectAttributes) {
        try {
            if (tripDTO.getTripId() != null) {
                tripService.updateTrip(tripDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Cap nhat chuyen xe thanh cong!");
            }
            return "redirect:/admin/trips";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/trips/details/" + tripDTO.getTripId();
        }
    }

    @GetMapping("/details/{id}")
    public String tripDetails(@PathVariable int id, Model model) {
        UpdateTripDTO updateTripDTO = new UpdateTripDTO();
        ResponseTripDTO tripResponse = tripService.getTripByIds(id);

        if (tripResponse.getTripsCount() == 1) {
            updateTripDTO = TripMapper.toUpdateDTO(tripResponse.getListTrips().get(0));
        }

        addTripFormOptions(model);
        model.addAttribute("updateTripForm", updateTripDTO);
        return "View/Admin/Trips/TripDetail";
    }

    private void addTripFormOptions(Model model) {
        List<RouteDTO> routes = routeService.getAllRoutes();
        List<DriverDTO> drivers = driverService.getAllDrivers();
        List<BusDTO> buses = busService.getAllBuses();

        model.addAttribute("routes", routes);
        model.addAttribute("drivers", drivers);
        model.addAttribute("buses", buses);
    }
}
