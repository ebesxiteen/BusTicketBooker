package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Routes.AddRouteDTO;
import com.example.ticketbooker.DTO.Routes.RouteDTO;
import com.example.ticketbooker.DTO.Routes.UpdateRouteDTO;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Util.Enum.RouteStatus;
import com.example.ticketbooker.Util.Mapper.RouteMapper;

class RouteMapperTest {

    @Test
    void fromAddAndToDTOMapExpectedFields() {
        AddRouteDTO addDto = new AddRouteDTO();
        addDto.setDepartureLocation("City A");
        addDto.setArrivalLocation("City B");
        addDto.setEstimatedTime(LocalTime.of(2, 30));
        addDto.setStatus(RouteStatus.ACTIVE);

        Routes route = RouteMapper.fromAdd(addDto);
        RouteDTO dto = RouteMapper.toDTO(route);

        assertEquals("City A", dto.getDepartureLocation());
        assertEquals("City B", dto.getArrivalLocation());
        assertEquals(LocalTime.of(2, 30), dto.getEstimatedTime());
        assertEquals(RouteStatus.ACTIVE, dto.getStatus());
    }

    @Test
    void fromUpdateAndToUpdateDTOPreserveIdentifiers() {
        UpdateRouteDTO updateDto = UpdateRouteDTO.builder()
                .routeId(11)
                .departureLocation("Origin")
                .arrivalLocation("Destination")
                .estimatedTime(LocalTime.of(3, 0))
                .status(RouteStatus.INACTIVE)
                .build();

        Routes updatedRoute = RouteMapper.fromUpdate(11, updateDto);
        UpdateRouteDTO roundTripDto = RouteMapper.toUpdateDTO(updatedRoute);

        assertEquals(11, roundTripDto.getRouteId());
        assertEquals("Origin", roundTripDto.getDepartureLocation());
        assertEquals("Destination", roundTripDto.getArrivalLocation());
        assertEquals(LocalTime.of(3, 0), roundTripDto.getEstimatedTime());
        assertEquals(RouteStatus.INACTIVE, roundTripDto.getStatus());
    }
}
