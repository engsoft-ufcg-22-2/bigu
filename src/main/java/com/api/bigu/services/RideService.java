package com.api.bigu.services;

import com.api.bigu.dto.candidate.CandidateMapper;
import com.api.bigu.dto.candidate.CandidateRequest;
import com.api.bigu.dto.candidate.CandidateResponse;
import com.api.bigu.dto.ride.RideMapper;
import com.api.bigu.dto.ride.RideRequest;
import com.api.bigu.dto.ride.RideResponse;
import com.api.bigu.dto.user.UserResponse;
import com.api.bigu.exceptions.*;
import com.api.bigu.models.Candidate;
import com.api.bigu.models.Ride;
import com.api.bigu.models.User;
import com.api.bigu.repositories.RideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class RideService {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private RideMapper rideMapper;

    public User getDriver(Integer userId) throws UserNotFoundException, NoCarsFoundException, CarNotFoundException {
        User user;
        user = userService.findUserById(userId);
        if (carService.findCarsByUserId(userId).isEmpty()) {
            throw new NoCarsFoundException("teste.");
        } else return user;
    }

    public User getUser(Integer userId) throws UserNotFoundException {
        User user;
        user = userService.findUserById(userId);
        return user;
    }


    public RideResponse createRide(RideRequest rideRequest, User driver) throws CarNotFoundException, InvalidTimeException {
        if (rideRequest.getDateTime().isBefore(LocalDateTime.now())) throw new InvalidTimeException("Data ou hora inválida.");
        Ride ride = rideMapper.toRide(rideRequest);
        Integer carId = rideRequest.getCarId();
        List<User> members = new ArrayList<>();
        if (Objects.equals(driver.getSex(), "M")) ride.setToWomen(false);
        ride.setCar(carService.findCarById(carId).get());
        members.add(driver);
        ride.setMembers(members);
        ride.setIsOver(false);
        ride.setDriverId(driver.getUserId());
        rideRepository.save(ride);
        userService.addRideToUser(driver.getUserId(), ride);
        return rideMapper.toRideResponse(ride);
    }

    public Ride findRideById(Integer rideId) throws RideNotFoundException {

        if (rideRepository.findById(rideId).isPresent()) {
            return rideRepository.findById(rideId).get();
        }
        else {
            throw new RideNotFoundException("Corrida não encontrada.");
        }
    }

    public void deleteRideById(Integer rideId) throws RideNotFoundException {
        rideRepository.findById(rideId).orElseThrow();

        rideRepository.deleteById(rideId);

    }

    public List<RideResponse> getAllRides() {
        List<Ride> rides = rideRepository.findAll();
        List<RideResponse> allRides = new ArrayList<>();
        for (Ride ride: rides) {
                    allRides.add(rideMapper.toRideResponse(ride));

        }
        return allRides;
    }

    public List<RideResponse> getMyRides(Integer userId) {
        List<Ride> rides = rideRepository.findAll();
        List<RideResponse> myRides = new ArrayList<>();
        for (Ride ride: rides) {
            if(ride.getDriverId().equals(userId))
                myRides.add(rideMapper.toRideResponse(ride));

        }
        return myRides;
    }

    public List<UserResponse> getRideMembers(Integer rideId) throws RideNotFoundException {
        Optional<Ride> ride = rideRepository.findById(rideId);
        List<UserResponse> members = new ArrayList<>();
        if (ride.isPresent()) {
            for (User member: ride.get().getMembers()
                 ) {
                members.add(userService.toResponse(member));
            }
        }
        return members;
    }

    public UserResponse getRideMember(Integer rideId, Integer userId) throws UserNotFoundException, RideNotFoundException {
        List<UserResponse> members = this.getRideMembers(rideId);
        for (UserResponse user : members) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        } throw new UserNotFoundException("Usuário não está na carona.");
    }
    public CandidateResponse requestRide(Integer userId, CandidateRequest candidateRequest) throws UserNotFoundException, RideIsFullException, AddressNotFoundException {
        Candidate candidate = candidateService.createCandidate(userId, candidateRequest);
        CandidateResponse candidateResponse = candidateMapper.toCandidateResponse(candidate);
        Ride ride = rideRepository.findById(candidateRequest.getRideId()).get();
        if (ride.getNumSeats() > ride.getMembers().size() - 1) {
            ride.getCandidates().add(candidate);
        } else throw new RideIsFullException("A carona está cheia.");
        return candidateResponse;
    }

    public RideResponse acceptCandidate(CandidateResponse candidateResponse) throws RideNotFoundException, UserNotFoundException {
        Ride ride = rideRepository.findById(candidateResponse.getRideId()).get();
        List<Candidate> candidates = ride.getCandidates().stream().toList();

        for (Candidate candidate: candidates) {
            if (candidate.getUserId().equals(candidateResponse.getUserResponse().getUserId())){
                if (candidateResponse.isAccepted()){
                    ride.getMembers().add(userService.findUserById(candidate.getUserId()));
                }
                candidateService.removeCandidate(candidate.getCandidateId());
            }
        }
        return rideMapper.toRideResponse(ride);
    }

    public List<RideResponse> findAvailableRidesToUser(Integer userId) throws UserNotFoundException {
        List<Ride> rides = rideRepository.findAll();
        boolean isWomen = getUser(userId).getSex().equals("F");
        List<RideResponse> availableRides = new ArrayList<>();
        for (Ride ride: rides) {
            System.err.println(ride.toString());
            if ((ride.getMembers().size() - 1 < ride.getNumSeats() && ride.getScheduledTime().isAfter(LocalDateTime.now()))){
                if(isWomen || !ride.getToWomen()){
                   availableRides.add(rideMapper.toRideResponse(ride));
                }
            }
        }
        return availableRides;
    }

    public List<RideResponse> findAllAvailableRides() throws UserNotFoundException {
        List<Ride> rides = rideRepository.findAll();
        List<RideResponse> availableRides = new ArrayList<>();
        for (Ride ride: rides) {
            if ((ride.getMembers().size() - 1 < ride.getNumSeats() && ride.getScheduledTime().isAfter(LocalDateTime.now()))){
                availableRides.add(rideMapper.toRideResponse(ride));
            }
        }
        return availableRides;
    }

    public List<RideResponse> getMemberHistory(Integer userId) throws UserNotFoundException {
        List<RideResponse> userHistory = new ArrayList<>();
        List<Ride> userRides = getUser(userId).getRides();
        for (Ride ride: userRides) {
            if (ride.getScheduledTime().isBefore(LocalDateTime.now())){
                userHistory.add(rideMapper.toRideResponse(ride));
            }
        }
        return userHistory;
    }

    public List<CandidateResponse> getCandidates(Integer driverId) throws RideNotFoundException, AddressNotFoundException {
        List<CandidateResponse> candidatesResponse = new ArrayList<>();
        List<Candidate> candidates = new ArrayList<>();
        List<Ride> userRides = userService.getRidesFromUser(driverId);
        for (Ride ride : userRides) {
            candidates.addAll(candidateService.getCandidatesFromRide(ride.getRideId()));
        }

        for(Candidate candidate: candidates) {
            candidatesResponse.add(candidateMapper.toCandidateResponse(candidate));
        }
        return candidatesResponse;
    }

    public RideResponse setRideOver(Integer rideId) throws RideNotFoundException{
        Ride ride = rideRepository.findById(rideId).get();
        ride.setIsOver(true);
        rideRepository.save(ride);
        return rideMapper.toRideResponse(ride);
    }
}
