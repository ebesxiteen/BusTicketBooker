package com.example.ticketbooker.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Routes.AddRouteDTO;
import com.example.ticketbooker.DTO.Routes.RequestRouteIdDTO;
import com.example.ticketbooker.DTO.Routes.ResponseRouteDTO;
import com.example.ticketbooker.DTO.Routes.RouteDTO;
import com.example.ticketbooker.DTO.Routes.SearchRouteRequest;
import com.example.ticketbooker.DTO.Routes.UpdateRouteDTO;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Util.Enum.RouteStatus;

public interface RouteService {
    public boolean addRoute(AddRouteDTO dto);
    public boolean updateRoute(UpdateRouteDTO dto);
    public boolean deleteRoute(RequestRouteIdDTO dto);
    public Routes getRoute(Integer id);
    public ResponseRouteDTO findAllRoutes();
    public Page<RouteDTO> findAllRoutes(Pageable pageable);
    public ResponseRouteDTO findByStatus(RouteStatus status);
    public ResponseRouteDTO findByDepartureLocation(String departureLocation);
    public ResponseRouteDTO findByArrivalLocation(String arrivalLocation);
    public ResponseRouteDTO findByLocation(String arrivalLocation);
    public ResponseRouteDTO findByLocations(SearchRouteRequest request);
    public List<RouteDTO> getAllRoutes();
    Page<RouteDTO> searchRoutes(String keyword, Pageable pageable);
    Page<RouteDTO> searchRoutesByStatus(String keyword, String statusStr, Pageable pageable);
}
