package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Seats.AddSeatDTO;
import com.example.ticketbooker.Entity.Trips;
import com.example.ticketbooker.Util.Mapper.SeatsMapper;

class SeatsMapperTest {

    private final SeatsMapper seatsMapper = new SeatsMapper();

    @Test
    void toEntitySetsTripAndSeatCode() {
        AddSeatDTO dto = AddSeatDTO.builder()
                .tripId(10)
                .seatCode("B05")
                .build();

        Trips trip = new Trips();
        trip.setId(10);

        var seat = seatsMapper.toEntity(dto, trip);

        assertEquals(trip, seat.getTrip());
        assertEquals("B05", seat.getSeatCode());
    }
}
