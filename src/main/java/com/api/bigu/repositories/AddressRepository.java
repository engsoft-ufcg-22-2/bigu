package com.api.bigu.repositories;

import com.api.bigu.models.Address;
import com.api.bigu.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    Optional<Address> findByPostalCode(Long postalCode);
}
