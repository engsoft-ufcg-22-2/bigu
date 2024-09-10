package com.api.bigu.dto.ride;

import com.api.bigu.dto.address.AddressResponse;
import com.api.bigu.dto.car.CarResponse;
import com.api.bigu.dto.user.UserResponse;
import com.api.bigu.models.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {

    private Integer id;

    private boolean goingToCollege;

    private UserResponse driver;

    private List<UserResponse> riders;

    private AddressResponse start;

    private AddressResponse destination;

    private LocalDateTime dateTime;

    private int numSeats;

    private double price;

    private boolean toWomen;

    private CarResponse car;

    private String description;

    private Boolean isOver;
}
