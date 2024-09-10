package com.api.bigu.dto.ride;

import com.api.bigu.models.Ride;
import com.api.bigu.repositories.AddressRepository;
import com.api.bigu.dto.address.AddressMapper;
import com.api.bigu.dto.car.CarMapper;
import com.api.bigu.dto.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RideMapper {

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    AddressMapper addressMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    CarMapper carMapper;

    public Ride toRide(RideRequest rideRequest) {
        return Ride.builder()
                .startAddress(addressRepository.findById(rideRequest.getStartAddressId()).get())
                .destinationAddress(addressRepository.findById(rideRequest.getDestinationAddressId()).get())
                .numSeats(rideRequest.getNumSeats())
                .goingToCollege(rideRequest.getGoingToCollege())
                .price(rideRequest.getPrice())
                .scheduledTime(rideRequest.getDateTime())
                .description(rideRequest.getDescription())
                .toWomen(rideRequest.getToWomen())
                .build();
    }

    public RideResponse toRideResponse(Ride rideCreated) {
        return RideResponse.builder()
                .id(rideCreated.getRideId())
                .goingToCollege(rideCreated.getGoingToCollege())
                .driver(userMapper.toUserResponse(rideCreated.getMembers().get(0)))
                .start(addressMapper.toAddressResponse(rideCreated.getStartAddress()))
                .destination(addressMapper.toAddressResponse(rideCreated.getDestinationAddress()))
                .dateTime(rideCreated.getScheduledTime())
                .numSeats(rideCreated.getNumSeats())
                .price(rideCreated.getPrice())
                .toWomen(rideCreated.getToWomen())
                .car(carMapper.toCarResponse(rideCreated.getCar()))
                .description(rideCreated.getDescription())
                .isOver(rideCreated.getIsOver())
                .riders(rideCreated.getMembers().stream().map(user -> userMapper.toUserResponse(user)).filter(userResponse -> !Objects.equals(userResponse.getUserId(), rideCreated.getDriverId())).collect(Collectors.toList()))
                .build();
    }
}
