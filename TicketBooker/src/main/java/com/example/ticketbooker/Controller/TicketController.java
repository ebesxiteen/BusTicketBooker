package com.example.ticketbooker.Controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ticketbooker.DTO.Ticket.TicketIdRequest;
import com.example.ticketbooker.DTO.Ticket.TicketResponse;
import com.example.ticketbooker.DTO.Ticket.UpdateTicketRequest;
import com.example.ticketbooker.DTO.Trips.ResponseTripDTO;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Service.TicketService;
import com.example.ticketbooker.Service.TripService;
import com.example.ticketbooker.Util.Mapper.TicketMapper;

@Controller
@RequestMapping("/admin/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TripService tripService;

    // 1. Hiển thị danh sách vé
    @GetMapping
    public String allTickets(Model model, 
                            @PageableDefault(size = 10) Pageable pageable, 
                            @RequestParam(value = "tripId", required = false) Integer tripId) {
        
        ResponseTripDTO responseTripDTO = tripService.getAllTrips();
        List<Trips> trips = responseTripDTO.getListTrips();
        TicketResponse ticketResponse;

        if (tripId != null) {
            ticketResponse = ticketService.getTicketsByTripId(tripId, pageable);
        } else {
            ticketResponse = ticketService.getAllTickets(pageable);
        }

        model.addAttribute("trips", trips);
        model.addAttribute("ticketResponse", ticketResponse);
        return "View/Admin/Tickets/ListTicket";
    }

    // 2. Xem chi tiết / Sửa vé
    @GetMapping("/details/{id}")
    public String ticketDetails(@PathVariable int id, Model model) {
        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        TicketIdRequest ticketIdRequest = new TicketIdRequest(id);
        
        TicketResponse response = ticketService.getTicketById(ticketIdRequest);

        if(response.getTicketsCount() == 1){
            // Dòng này giờ sẽ HẾT LỖI nhờ hàm Mapper mới thêm
            updateRequest = TicketMapper.toUpdateDTO(response.getListTickets().get(0));
        }
        
        model.addAttribute("updateUserForm", updateRequest); // Lưu ý: Tên model attribute là updateUserForm? Nên đổi thành updateTicketForm cho đỡ nhầm
        return "View/Admin/Tickets/TicketDetails";
    }

    // 3. Xuất Excel
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportTicketsToExcel(@RequestParam(value = "tripId", required = false) Integer tripId) {
        ByteArrayInputStream in;

        if (tripId != null) {
            in = ticketService.exportTicketsToExcelByTripId(tripId);
        } else {
            in = ticketService.exportAllTicketsToExcel();
        }

        String fileName = (tripId != null) ? "tickets_trip_" + tripId + ".xlsx" : "tickets_all.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
}