package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.ticketbooker.DTO.Routes.AddRouteDTO;
import com.example.ticketbooker.DTO.Routes.RouteDTO;
import com.example.ticketbooker.Entity.Routes;
import com.example.ticketbooker.Repository.RouteRepo;
import com.example.ticketbooker.Service.ServiceImp.RouteServiceImp;
import com.example.ticketbooker.Util.Enum.RouteStatus;

@ExtendWith(MockitoExtension.class)
class RouteServiceImpTest {

    @Mock
    private RouteRepo routeRepo;

    @InjectMocks
    private RouteServiceImp routeServiceImp;

    @Test
    void addRouteMapsRequestAndSavesEntity() {
        AddRouteDTO request = new AddRouteDTO("Ha Noi", "Da Nang", "10:30", RouteStatus.ACTIVE);
        ArgumentCaptor<Routes> routeCaptor = ArgumentCaptor.forClass(Routes.class);

        boolean result = routeServiceImp.addRoute(request);

        assertTrue(result);
        verify(routeRepo).save(routeCaptor.capture());
        Routes savedRoute = routeCaptor.getValue();
        assertEquals("Ha Noi", savedRoute.getDepartureLocation());
        assertEquals("Da Nang", savedRoute.getArrivalLocation());
        assertEquals(LocalTime.of(10, 30), savedRoute.getEstimatedTime());
        assertEquals(RouteStatus.ACTIVE, savedRoute.getStatus());
    }

    @Test
    void getRouteReturnsRepositoryResult() {
        Routes route = new Routes(3, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeRepo.findById(3)).thenReturn(Optional.of(route));

        Routes result = routeServiceImp.getRoute(3);

        assertSame(route, result);
    }

    @Test
    void searchRoutesUsesKeywordSearchWhenKeywordHasText() {
        Pageable pageable = PageRequest.of(0, 10);
        Routes route = new Routes(3, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeRepo.searchByKeyword("Da", pageable)).thenReturn(new PageImpl<>(List.of(route), pageable, 1));

        Page<RouteDTO> result = routeServiceImp.searchRoutes("Da", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Da Nang", result.getContent().get(0).getArrivalLocation());
        verify(routeRepo).searchByKeyword("Da", pageable);
    }

    @Test
    void searchRoutesUsesFindAllWhenKeywordIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Routes route = new Routes(3, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeRepo.findAll(pageable)).thenReturn(new PageImpl<>(List.of(route), pageable, 1));

        Page<RouteDTO> result = routeServiceImp.searchRoutes("   ", pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepo).findAll(pageable);
    }

    @Test
    void searchRoutesByStatusPassesParsedStatusToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Routes route = new Routes(3, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeRepo.findWithFilter("Ha", RouteStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(route), pageable, 1));

        Page<RouteDTO> result = routeServiceImp.searchRoutesByStatus("Ha", "ACTIVE", pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepo).findWithFilter("Ha", RouteStatus.ACTIVE, pageable);
    }

    @Test
    void searchRoutesByStatusTreatsInvalidStatusAsNoStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Routes route = new Routes(3, "Ha Noi", "Da Nang", LocalTime.of(10, 30), RouteStatus.ACTIVE);
        when(routeRepo.findWithFilter(eq("Ha"), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(route), pageable, 1));

        Page<RouteDTO> result = routeServiceImp.searchRoutesByStatus("Ha", "UNKNOWN", pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepo).findWithFilter("Ha", null, pageable);
    }
}
