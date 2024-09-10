package com.api.bigu.controllers;

import com.api.bigu.config.JwtService;
import com.api.bigu.dto.candidate.CandidateRequest;
import com.api.bigu.dto.candidate.CandidateResponse;
import com.api.bigu.dto.ride.RideRequest;
import com.api.bigu.dto.ride.RideResponse;
import com.api.bigu.dto.user.UserResponse;
import com.api.bigu.exceptions.*;
import com.api.bigu.models.User;
import com.api.bigu.dto.candidate.CandidateMapper;
import com.api.bigu.services.CarService;
import com.api.bigu.dto.ride.RideMapper;
import com.api.bigu.services.RideService;
import com.api.bigu.util.errors.AddressError;
import com.api.bigu.util.errors.CarError;
import com.api.bigu.util.errors.RideError;
import com.api.bigu.util.errors.UserError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    @Autowired
    CandidateMapper candidateMapper;

    @Autowired
    RideService rideService;

    @Autowired
    CarService carService;

    @Autowired
    JwtService jwtService;

    @Autowired
    RideMapper rideMapper;

    @GetMapping
    public ResponseEntity<?> getAllRides(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId));
            List<RideResponse> allRides = rideService.getAllRides();
            return ResponseEntity.ok(allRides);
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        }

    }

    @GetMapping("/my-rides")
    public ResponseEntity<?> getMyRides(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId));
            List<RideResponse> myRides = rideService.getMyRides(userId);
            return ResponseEntity.ok(myRides);
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        }

    }

    @GetMapping("/{rideId}")
    public ResponseEntity<?> searchById(@PathVariable Integer rideId) {
        try {
            RideResponse ride = rideMapper.toRideResponse(rideService.findRideById(rideId));
            return ResponseEntity.ok(ride);
        } catch (RideNotFoundException rNFE) {
            return RideError.rideNotFoundError();
        }
    }

    @GetMapping("/{rideId}/members")
    public ResponseEntity<?> getRideMembers(@PathVariable Integer rideId) {
        try {
            List<UserResponse> members = rideService.getRideMembers(rideId);
            return ResponseEntity.ok(members);
        } catch (RideNotFoundException rNFE) {
            return RideError.rideNotFoundError();
        }
    }

    @GetMapping("/{rideId}/{memberId}")
    public ResponseEntity<?> getRideMember(@PathVariable Integer rideId, @PathVariable Integer memberId) {
        try {
            UserResponse member = rideService.getRideMember(rideId, memberId);
            return ResponseEntity.ok(member);
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (RideNotFoundException rNFE) {
            return RideError.rideNotFoundError();
        }
    }


    @PostMapping()
    public ResponseEntity<?> createRide(@RequestHeader("Authorization") String authorizationHeader, @RequestBody RideRequest rideRequest) {
        try {
            Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            User driver = rideService.getDriver(userId);
            RideResponse rideResponse = rideService.createRide(rideRequest, driver);
            return ResponseEntity.ok(rideResponse);
        } catch (CarNotFoundException cNFE) {
            return CarError.carNotFoundError();
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (NoCarsFoundException nCFE) {
            return CarError.noCarsFoundError();
        } catch (InvalidTimeException e) {
            return RideError.invalidDateTimeError();
        }
    }

    @PutMapping("/request-ride")
    public ResponseEntity<?> requestRide(@RequestHeader("Authorization") String authorizationHeader, @RequestBody CandidateRequest candidateRequest) {
        CandidateResponse candidateResponse = new CandidateResponse();
        try {
            Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            User rider = rideService.getUser(userId);
            if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), rider)) {
                candidateResponse = rideService.requestRide(userId, candidateRequest);
            }

        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (RideIsFullException rIFE) {
            return RideError.rideIsFullError();
        } catch (AddressNotFoundException e) {
            return AddressError.addressNotFoundError();
        }
        return ResponseEntity.ok(candidateResponse);
    }

    @PutMapping("/answer-candidate")
    public ResponseEntity<?> answerCandidate(@RequestHeader("Authorization") String authorizationHeader, @RequestBody CandidateResponse candidateResponse) {
        try {
            Integer driverId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            User driver = rideService.getDriver(driverId);
            if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), driver)) {
                return ResponseEntity.ok(rideService.acceptCandidate(candidateResponse));
            } else return UserError.userBlockedError();
        } catch (CarNotFoundException cNFE) {
            return CarError.carNotFoundError();
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (NoCarsFoundException nCFE) {
            return CarError.noCarsFoundError();
        } catch (RideNotFoundException rNFE) {
            return RideError.rideNotFoundError();
        }
    }

    @GetMapping("/candidates")
    public ResponseEntity<?> getCandidates(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Integer driverId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            User driver = rideService.getDriver(driverId);
            if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), driver)) {
                return ResponseEntity.ok(rideService.getCandidates(driverId));
            } else return UserError.userBlockedError();
        } catch (CarNotFoundException cNFE) {
            return CarError.carNotFoundError();
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (NoCarsFoundException nCFE) {
            return CarError.noCarsFoundError();
        } catch (RideNotFoundException rNFE) {
            return RideError.rideNotFoundError();
        } catch (AddressNotFoundException e) {
            return AddressError.addressNotFoundError();
        }
    }

    @DeleteMapping("/delete-ride/{rideId}")
    public ResponseEntity<?> cancelRide(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer rideId) {
        try {
            Integer driverId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
            User driver = rideService.getDriver(driverId);

            if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), driver)) {
                rideService.deleteRideById(rideId);
                return new ResponseEntity<>(HttpStatus.OK);
            } else return UserError.userBlockedError();

        } catch (CarNotFoundException cNFE) {
            return CarError.carNotFoundError();
        } catch (UserNotFoundException uNFE) {
            return UserError.userNotFoundError();
        } catch (NoCarsFoundException nCFE) {
            return CarError.noCarsFoundError();
        } catch (RideNotFoundException e) {
            return RideError.rideNotFoundError();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRidesToUser(@RequestHeader("Authorization") String authorizationHeader) {
        List<RideResponse> availableRides = new ArrayList<>();
        Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
        if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId))) {
            availableRides = rideService.findAvailableRidesToUser(userId);
        }

        return ResponseEntity.ok(availableRides);
    }

    @GetMapping("/all-available")
    public ResponseEntity<?> getAllAvailable(@RequestHeader("Authorization") String authorizationHeader) {
        List<RideResponse> availableRides = new ArrayList<>();
        Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
        if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId))) {
            availableRides = rideService.findAllAvailableRides();
        }

        return ResponseEntity.ok(availableRides);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMemberHistory(@RequestHeader("Authorization") String authorizationHeader) {
        Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
        jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId));
        List<RideResponse> memberRides = rideService.getMemberHistory(userId);
        return ResponseEntity.ok(memberRides);

    }

    @PutMapping("/over/{rideId}")
    public ResponseEntity<?> setRideOver(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Integer rideId){
        Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
        if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), rideService.getUser(userId))){
            try {
                rideService.setRideOver(rideId);
            } catch (RideNotFoundException e) {
                return RideError.rideNotFoundError();
            }
        }
        return ResponseEntity.ok("Ride " + rideId + " is over.");
    }
}

