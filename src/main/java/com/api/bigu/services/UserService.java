package com.api.bigu.services;

import com.api.bigu.dto.auth.RegisterRequest;
import com.api.bigu.dto.user.EditUserRequest;
import com.api.bigu.dto.user.UserMapper;
import com.api.bigu.dto.user.UserResponse;
import com.api.bigu.exceptions.RideNotFoundException;
import com.api.bigu.exceptions.UserNotFoundException;
import com.api.bigu.models.Address;
import com.api.bigu.models.Feedback;
import com.api.bigu.models.Ride;
import com.api.bigu.models.User;
import com.api.bigu.models.enums.Role;
import com.api.bigu.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Integer buildUser(RegisterRequest requestUser) {
        User user = User.builder()
                .fullName(requestUser.getFullName())
                .email(requestUser.getEmail())
                .phoneNumber(requestUser.getPhoneNumber())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .role(Role.valueOf(requestUser.getRole().toUpperCase()))
                .build();
        this.registerUser(user);
        return user.getUserId();
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Integer userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user by id: " + userId));
    }

    public UserResponse toResponse(User user) {
        return userMapper.toUserResponse(user);
    }

    public void deleteById(Integer userId) {
        userRepository.deleteById(userId);
    }

    public User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("No user by email: " + userEmail));
    }

    public boolean validateUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).get();
        if (userRepository.findByEmail(userEmail).isPresent()){
            user.setValidated(true);
            return true;
        }
        return false;
    }

    public void addAddressToUser(Address address, Integer userId){
        User user = userRepository.findById(userId).get();
        user.getAddresses().put(address.getNickname(), address);
    }

    public boolean isBlocked(String email) {
        return userRepository.findByEmail(email).get().isAccountNonLocked();
    }
    
    public void updateResetPasswordToken(String token, String email) throws UserNotFoundException {
    	
    	Optional<User> user = userRepository.findByEmail(email);
    	
    	if (user.isPresent()) {
            user.get().setResetPasswordToken(token);
            userRepository.save(user.get());
        } else {
            throw new UserNotFoundException("O usuário com email " + email + " não foi encontrado.");
        }
    }

    public void updateUserValidateToken(String token, String email) throws UserNotFoundException {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            user.get().setUserValidateToken(token);
            userRepository.save(user.get());
        } else {
            throw new UserNotFoundException("O usuário com email " + email + " não foi encontrado.");
        }
    }
    
    public Optional<User> findUserByResetPasswordToken(String token) throws UserNotFoundException {
    	Optional<User> user = userRepository.findByResetPasswordToken(token);
    	
    	if (user.isPresent()) {
            return user;
        } else {
            throw new UserNotFoundException("O usuário não foi encontrado.");
        }
    }

    public void updatePassword(Integer userId, String encodedNewPassword) {
        User user = userRepository.findById(userId).get();
        user.setPassword(encodedNewPassword);
    }

    public UserResponse editProfile(Integer userId, EditUserRequest editUserRequest) {
        User user = userRepository.findById(userId).get();
        user.setFullName(editUserRequest.getFullName());
        user.setEmail(editUserRequest.getEmail());
        user.setPhoneNumber(editUserRequest.getPhoneNumber());
        user.setMatricula(editUserRequest.getMatricula());
        return toResponse(user);
    }

    public void addRideToUser(Integer userId, Ride ride) {
        User user = userRepository.findById(userId).get();
        user.getRides().add(ride);
    }

    public List<Ride> getRidesFromUser(Integer userId) throws UserNotFoundException, RideNotFoundException {
        return userRepository.findById(userId).get().getRides();
    }

    public void saveUserProfileImage(User user, MultipartFile profileImage) throws IOException {
        if (profileImage != null && !profileImage.isEmpty()) {
            deleteUserProfileImage(user);
            user.setProfileImageName(profileImage.getOriginalFilename());
            user.setProfileImageType(profileImage.getContentType());
            user.setProfileImage(profileImage.getBytes());
            userRepository.save(user);
        }
    }

    public void deleteUserProfileImage(User user){
        user.setProfileImage(null);
        user.setProfileImageName(null);
        user.setProfileImageType(null);
        userRepository.save(user);
    }

    public void addFeedbackToUser(Integer userId, Feedback feedback){
        User user = userRepository.findById(userId).get();
        user.getFeedbacks().add(feedback);
        userRepository.save(user);
        if (feedback.getReceiverId().equals(userId)){
            user.setAvgScore(avgFeedbacksReceived(userId));
        }
    }

    public void deleteFeedbackFromUser(Integer userId, Feedback feedback){
        User user = userRepository.findById(userId).get();
        user.getFeedbacks().remove(feedback);
        userRepository.save(user);
        if (feedback.getReceiverId().equals(userId)){
            user.setAvgScore(avgFeedbacksReceived(userId));
        }
    }

    public float avgFeedbacksReceived(Integer userId) throws UserNotFoundException{
        int count = 0;
        float sumScore = 0;
        for (Feedback feedback: userRepository.findById(userId).get().getFeedbacks()) {
            if (userId.equals(feedback.getReceiverId())){
                sumScore += feedback.getScore();
                count++;
            }
        }
        float avg = sumScore/count;
        return avg;
    }

    public List<Feedback> getFeedbacksSent(Integer userId){
        List<Feedback> feedbacks = new ArrayList<>();
        for(Feedback feedback : userRepository.findById(userId).get().getFeedbacks()){
            if (userId.equals(feedback.getSenderId())){
                feedbacks.add(feedback);
            }
        }
        return feedbacks;
    }

    public List<Feedback> getFeedbacksReceived(Integer userId){
        List<Feedback> feedbacks = new ArrayList<>();
        for(Feedback feedback : userRepository.findById(userId).get().getFeedbacks()){
            if (userId.equals(feedback.getReceiverId())){
                feedbacks.add(feedback);
            }
        }
        return feedbacks;
    }
}
