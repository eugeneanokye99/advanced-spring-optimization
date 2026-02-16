package com.shopjoy.repository;

import com.shopjoy.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUserId(int userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(int userId);
    
    void deleteByUserId(int userId);
}
