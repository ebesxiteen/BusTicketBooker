package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ticketbooker.DTO.ChartData;
import com.example.ticketbooker.Repository.StatisticRepo;
import com.example.ticketbooker.Service.ServiceImp.StatisticsService;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticRepo statisticRepo;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void mapToChartDataUsesZeroWhenRevenueIsNull() {
        Date date = Date.valueOf(LocalDate.of(2026, 5, 29));

        List<ChartData> result = statisticsService.mapToChartData(List.<Object[]>of(new Object[] {date, 3L, 2L, null}));

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).getRevenue());
    }

    @Test
    void getStatisticsFetchesAndMapsRepositoryRows() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 29);
        Date date = Date.valueOf(start);
        when(statisticRepo.fetchStatistics(start, end)).thenReturn(List.<Object[]>of(new Object[] {date, 4L, 3L, BigDecimal.TEN}));

        List<ChartData> result = statisticsService.getStatistics(start, end);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.TEN, result.get(0).getRevenue());
        verify(statisticRepo).fetchStatistics(start, end);
    }

    @Test
    void countMethodsDelegateToRepository() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 29);
        when(statisticRepo.countAllUser()).thenReturn(10);
        when(statisticRepo.countOrders(start, end)).thenReturn(11);
        when(statisticRepo.countTickets(start, end)).thenReturn(12);
        when(statisticRepo.countCompletedOrders(start, end)).thenReturn(13);
        when(statisticRepo.countUncompletedOrders(start, end)).thenReturn(14);
        when(statisticRepo.countCancelledOrders(start, end)).thenReturn(15);

        assertEquals(10, statisticsService.countAllUser());
        assertEquals(11, statisticsService.countOrders(start, end));
        assertEquals(12, statisticsService.countTickets(start, end));
        assertEquals(13, statisticsService.countCompletedOrders(start, end));
        assertEquals(14, statisticsService.countUncompletedOrders(start, end));
        assertEquals(15, statisticsService.countCancelledOrders(start, end));
    }
}
