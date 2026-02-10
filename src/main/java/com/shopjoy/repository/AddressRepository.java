package com.shopjoy.repository;

import com.shopjoy.entity.Address;

import java.util.List;
import java.util.Optional;

public interface IAddressRepository extends GenericRepository<Address, Integer> {
    List<Address> findByUserId(int userId);
    Optional<Address> findDefaultAddress(int userId);
    void clearDefaultAddresses(int userId);
    Address setDefaultAddress(int addressId);
}
