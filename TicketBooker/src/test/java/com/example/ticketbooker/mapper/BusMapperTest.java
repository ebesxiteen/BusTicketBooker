package com.example.ticketbooker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.example.ticketbooker.DTO.Bus.BusDTO;
import com.example.ticketbooker.Entity.Buses;
import com.example.ticketbooker.Util.Enum.BusStatus;
import com.example.ticketbooker.Util.Enum.BusType;
import com.example.ticketbooker.Util.Mapper.BusMapper;

class BusMapperTest {

    @Test
    void toDTOMapsFieldsCorrectly() {
        Buses bus = new Buses(5, null, "51A-12345", BusType.SLEEPER, 40, BusStatus.MAINTENANCE);

        BusDTO dto = BusMapper.toDTO(bus);

        assertEquals(5, dto.getId());
        assertEquals("51A-12345", dto.getLicensePlate());
        assertEquals(BusType.SLEEPER, dto.getBusType());
        assertEquals(40, dto.getCapacity());
        assertEquals(BusStatus.MAINTENANCE, dto.getBusStatus());
    }

    @Test
    void toEntityMapsFieldsCorrectly() {
        BusDTO dto = BusDTO.builder()
                .id(3)
                .licensePlate("43B-99887")
                .busType(BusType.SEAT)
                .capacity(28)
                .busStatus(BusStatus.ACTIVE)
                .build();

        Buses entity = BusMapper.toEntity(dto);

        assertEquals(3, entity.getId());
        assertEquals("43B-99887", entity.getLicensePlate());
        assertEquals(BusType.SEAT, entity.getBusType());
        assertEquals(28, entity.getCapacity());
        assertEquals(BusStatus.ACTIVE, entity.getBusStatus());
    }

    @Test
    void mapperReturnsNullForNullInputs() {
        assertNull(BusMapper.toDTO(null));
        assertNull(BusMapper.toEntity(null));
    }
}
