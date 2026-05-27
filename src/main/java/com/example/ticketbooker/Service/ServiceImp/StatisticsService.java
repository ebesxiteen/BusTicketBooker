package com.example.ticketbooker.Service.ServiceImp;

import com.example.ticketbooker.DTO.ChartData;
import com.example.ticketbooker.Repository.StatisticRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticRepo statisticRepo;

    public List<ChartData> getStatistics(LocalDate startDate, LocalDate endDate) {

        List<Object[]> dataReturn = statisticRepo.fetchStatistics(startDate, endDate);
        System.out.println(dataReturn);
        return mapToChartData(dataReturn);
    }
    public List<ChartData> mapToChartData(List<Object[]> queryResult) {
        List<ChartData> chartDataList = new ArrayList<>();
        for (Object[] row : queryResult) {
            java.sql.Date paymentDate = (java.sql.Date) row[0]; // Assuming it's a LocalDate
            long totalTickets = (Long) row[1];
            long totalInvoices = (Long) row[2];
            BigDecimal totalRevenue = (BigDecimal) row[3];
            if (totalRevenue == null) {
                totalRevenue = BigDecimal.ZERO;
            }
//            LocalDate localPaymentDate = paymentDate.toLocalDate();
            ChartData chartData = new ChartData(paymentDate, totalTickets, totalInvoices, totalRevenue);
            chartDataList.add(chartData);
        }
        return chartDataList;
    }
    public int countAllUser(){
        return statisticRepo.countAllUser();
    }
    public int countOrders(LocalDate startDate, LocalDate endDate) {
        return statisticRepo.countOrders(startDate, endDate);
    }
    public int countTickets(LocalDate startDate, LocalDate endDate) {
        return statisticRepo.countTickets(startDate, endDate);
    }

    public int countCompletedOrders(LocalDate startDate, LocalDate endDate) {
        return statisticRepo.countCompletedOrders(startDate, endDate);
    }

    public int countUncompletedOrders(LocalDate startDate, LocalDate endDate) {
        return statisticRepo.countUncompletedOrders(startDate, endDate);
    }

    public int countCancelledOrders(LocalDate startDate, LocalDate endDate) {
        return statisticRepo.countCancelledOrders(startDate, endDate);
    }
}
