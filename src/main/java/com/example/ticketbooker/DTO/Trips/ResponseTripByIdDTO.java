package com.example.ticketbooker.DTO.Trips;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTripByIdDTO {

    private String departureLocation;
    private String arrivalLocation;
    private String departureTime;
    private String totalPrice;
    private Integer capacity;

}
