package com.example.ticketbooker.Controller.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.DTO.Driver.DriverDTO;
import com.example.ticketbooker.DTO.Driver.ResponseDriverDTO;
import com.example.ticketbooker.DTO.Driver.UpdateDriverDTO;
import com.example.ticketbooker.Service.DriverService;
import com.example.ticketbooker.Util.Mapper.DriverMapper;

@RestController
@RequestMapping("/api/drivers")
public class DriverApi {
    @Autowired
    private DriverService driverService;
   @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id){
        try{
            boolean result = driverService.deleteDriver(id);
            if(result) return ResponseEntity.ok().build();
            else return ResponseEntity.badRequest().body("Không thể xóa (Ràng buộc dữ liệu)");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/search")
    public ResponseDriverDTO searchUser(@RequestBody String searchTerm) {
        ResponseDriverDTO response;
        try {
            response = this.driverService.findAllField(searchTerm);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        return response;
    }

    @GetMapping("/getAll")
    public ResponseDriverDTO getAllDrivers() {
        ResponseDriverDTO drivers;
        try{
            drivers = this.driverService.findAll();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return drivers;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateDriverStatus(@PathVariable Integer id, @RequestBody DriverDTO driverDTO, Model model){
        UpdateDriverDTO updateDriverDTO = DriverMapper.toUpdateDTO(driverService.getDriver(id));
        if(updateDriverDTO != null){
            try{
                System.out.println("Check inside update driver status:  "+updateDriverDTO);
                updateDriverDTO.setDriverStatus(driverDTO.getDriverStatus());
                driverService.updateDriver(updateDriverDTO);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
