package com.api.bigu;
import com.api.bigu.dto.car.CarMapper;
import com.api.bigu.dto.car.CarRequest;
import com.api.bigu.dto.ride.RideRequest;
import com.api.bigu.exceptions.CarNotFoundException;
import com.api.bigu.exceptions.InvalidTimeException;
import com.api.bigu.exceptions.UserNotFoundException;
import com.api.bigu.models.Address;
import com.api.bigu.models.Ride;
import com.api.bigu.models.User;
import com.api.bigu.models.enums.Role;
import com.api.bigu.repositories.AddressRepository;
import com.api.bigu.repositories.CarRepository;
import com.api.bigu.services.AuthenticationService;
import com.api.bigu.repositories.UserRepository;
import com.api.bigu.services.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class BiguApplication {
    private final UserRepository userRepository;

    public BiguApplication(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    public static void main(String[] args) {
        SpringApplication.run(BiguApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(AuthenticationService authService, AddressRepository addressRepository, UserService userService, CarService carService, RideService rideService, CarMapper carMapper, CarRepository carRepository) {
        return args -> {
            var driver = User.builder()
                    .fullName("Driver Fuch")
                    .email("driver@mail.ufcg.edu.br")
                    .sex("M")
                    .phoneNumber("12312312123")
                    .password("123456")
                    .role(Role.USER)
                    .isValidated(true)
                    .build();
            System.err.println("Driver Registered");
            System.err.println("Driver token: " + authService.registerMock(driver).getToken());

            var rider = User.builder()
                    .fullName("Rider Ramalho")
                    .email("rider@mail.ufcg.edu.br")
                    .sex("M")
                    .phoneNumber("222222222")
                    .password("1234")
                    .role(Role.USER)
                    .isValidated(true)
                    .build();
            System.err.println("Rider registered");
            System.err.println("Rider token: " + authService.registerMock(rider).getToken());

            var driverF = User.builder()
                    .fullName("Driver Alves")
                    .email("driver.alves@mail.ufcg.edu.br")
                    .sex("F")
                    .phoneNumber("8388876616")
                    .password("12345")
                    .role(Role.USER)
                    .isValidated(true)
                    .build();
            System.err.println("Driver F registered");
            System.err.println("Driver F token: " + authService.registerMock(driverF).getToken());

            var riderF = User.builder()
                    .fullName("Rider Female")
                    .email("rider.female@mail.ufcg.edu.br")
                    .sex("F")
                    .phoneNumber("12312312312")
                    .password("12345")
                    .role(Role.USER)
                    .isValidated(true)
                    .build();
            System.err.println("Rider F registered");
            System.err.println("Rider F token: " + authService.registerMock(riderF).getToken());


            /*var rider = RegisterRequest.builder()
                    .fullName("Rider1 Silva")
                    .email("matheus.rafael@ccc.ufcg.edu.br")
                    .sex("F")
                    .phoneNumber("333333333")
                    .password("1234")
                    .role("USER")
                    .build();
            System.err.println("User 3 registered");
            System.err.println("User 3 token: " + authService.register(rider).getToken());

            var rider2 = RegisterRequest.builder()
                    .fullName("Rider2 Sousa")
                    .email("rider2@mail.ufcg.edu.br")
                    .sex("M")
                    .phoneNumber("8399945785")
                    .password("12345")
                    .role("USER")
                    .build();
            System.err.println("User 4 registered");
            System.err.println("User 4 token: " + authService.register(rider2).getToken());*/


            CarRequest carDriver = CarRequest.builder()
                    .brand("Chevrolet")
                    .plate("KGU7E07")
                    .color("Prata")
                    .model("Onix")
                    .modelYear("2019")
                    .build();
            try {
                carService.addCarToUser(driver.getUserId(), carDriver);
            } catch (UserNotFoundException e) {
                throw new NullPointerException("Usuário não encontrado.");
            }
            System.err.println("Car registered");

            CarRequest carDriverF = CarRequest.builder()
                    .brand("Hyundai")
                    .plate("MNH9728")
                    .color("Branco")
                    .model("HB20")
                    .modelYear("2021")
                    .build();
            try {
                carService.addCarToUser(driverF.getUserId(), carDriverF);
            } catch (UserNotFoundException e) {
                throw new NullPointerException("Usuário não encontrado.");
            }
            System.err.println("Car registered");

            Address addressUFCG1 = Address.builder()
                    .nickname("UFCG - Frente")
                    .postalCode("58429900")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Universitário")
                    .street("Rua Aprígio Veloso")
                    .number("882")
                    .complement("Entrada principal da UFCG")
                    .build();
            addressRepository.save(addressUFCG1);

            Address addressUFCG2 = Address.builder()
                    .nickname("UFCG - CEEI")
                    .postalCode("58429900")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Universitário")
                    .street("Rua Aprígio Veloso")
                    .number("882")
                    .complement("Entrada do Centro de Engenharia Elétrica e Informática")
                    .build();
            addressRepository.save(addressUFCG2);

            Address addressUFCG3 = Address.builder()
                    .nickname("UFCG - Humanas")
                    .postalCode("58429900")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Universitário")
                    .street("Rua Aprígio Veloso")
                    .number("882")
                    .complement("Entrada por trás do bloco BG")
                    .build();
            addressRepository.save(addressUFCG3);

            Address addressDriver = Address.builder()
                    .nickname("Casa")
                    .postalCode("58433264")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Malvinas")
                    .street("Rua Exemplo")
                    .number("284")
                    .userId(userRepository.findByEmail(driver.getEmail()).get().getUserId())
                    .build();
            addressRepository.save(addressDriver);
            userService.addAddressToUser(addressDriver, (userRepository.findByEmail(driver.getEmail()).get()).getUserId());


            Address addressDriver2 = Address.builder()
                    .nickname("Casa de pai")
                    .postalCode("58426542")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Liberdade")
                    .street("Rua das Coisas")
                    .number("111")
                    .userId(userRepository.findByEmail(driverF.getEmail()).get().getUserId())
                    .build();
            addressRepository.save(addressDriver2);
            userService.addAddressToUser(addressDriver2, (userRepository.findByEmail(driverF.getEmail()).get()).getUserId());


            Address addressRider = Address.builder()
                    .nickname("Trabalho")
                    .postalCode("58400165")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Centro")
                    .street("Rua Teste")
                    .number("123")
                    .userId(userRepository.findByEmail(rider.getEmail()).get().getUserId())
                    .build();
            addressRepository.save(addressRider);
            userService.addAddressToUser(addressRider, userRepository.findByEmail(rider.getEmail()).get().getUserId());


            Address addressRider2 = Address.builder()
                    .nickname("Casa de mãe")
                    .postalCode("58787455")
                    .state("PB")
                    .city("Campina Grande")
                    .district("Alto Branco")
                    .street("Av. Manoel Tavares")
                    .number("1000")
                    .userId(userRepository.findByEmail(riderF.getEmail()).get().getUserId())
                    .build();
            addressRepository.save(addressRider2);
            userService.addAddressToUser(addressRider2, userRepository.findByEmail(riderF.getEmail()).get().getUserId());

        };
    }
}
