package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.AddressMapperStruct;
import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.request.UpdateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.entity.Address;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.AddressRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.AddressService;
import com.shopjoy.util.SecurityUtil;

import lombok.AllArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Address service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapperStruct addressMapper;
    
    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "addresses", key = "#request.userId"),
        @CacheEvict(value = "defaultAddress", key = "#request.userId")
    })
    public AddressResponse createAddress(CreateAddressRequest request) {
        Address address = addressMapper.toAddress(request);
        address.setUser(userRepository.getReferenceById(request.getUserId()));
        address.setCreatedAt(LocalDateTime.now());
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(savedAddress);
    }
    
    @Override
    @Cacheable(value = "addresses", key = "#addressId", cacheManager = "mediumCacheManager")
    public AddressResponse getAddressById(Integer addressId) {
        Integer addressOwnerId = addressRepository.findUserIdByAddressId(addressId);
        if (addressOwnerId == null) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(addressOwnerId)) {
            throw new AccessDeniedException("You do not have permission to access this address");
        }
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return addressMapper.toAddressResponse(address);
    }
    
    @Override
    @Cacheable(value = "addresses", key = "'user_' + #userId", cacheManager = "mediumCacheManager")
    public List<AddressResponse> getAddressesByUser(Integer userId) {

        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    @Caching(
        put = { @CachePut(value = "addresses", key = "#addressId", cacheManager = "mediumCacheManager") },
        evict = {
            @CacheEvict(value = "addresses", allEntries = true),
            @CacheEvict(value = "defaultAddress", allEntries = true)
        }
    )
    public AddressResponse updateAddress(Integer addressId, UpdateAddressRequest request) {
        Integer addressOwnerId = addressRepository.findUserIdByAddressId(addressId);
        if (addressOwnerId == null) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(addressOwnerId)) {
            throw new AccessDeniedException("You do not have permission to update this address");
        }
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        addressMapper.updateAddressFromRequest(request, address);
        
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(updatedAddress);
    }
    
    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = "addresses", allEntries = true),
        @CacheEvict(value = "defaultAddress", allEntries = true)
    })
    public void deleteAddress(Integer addressId) {
        Integer addressOwnerId = addressRepository.findUserIdByAddressId(addressId);
        if (addressOwnerId == null) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(addressOwnerId)) {
            throw new AccessDeniedException("You do not have permission to delete this address");
        }
        
        addressRepository.deleteById(addressId);
    }
    
    @Override
    @Transactional()
    @Caching(
        put = { @CachePut(value = "addresses", key = "#addressId", cacheManager = "mediumCacheManager") },
        evict = {
            @CacheEvict(value = "addresses", allEntries = true),
            @CacheEvict(value = "defaultAddress", allEntries = true)
        }
    )
    public AddressResponse setDefaultAddress(Integer addressId) {
        Integer addressOwnerId = addressRepository.findUserIdByAddressId(addressId);
        if (addressOwnerId == null) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isCurrentUser(addressOwnerId)) {
            throw new AccessDeniedException("You do not have permission to set this address as default");
        }
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        addressRepository.findDefaultByUserId(address.getUser().getId())
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    addressRepository.save(currentDefault);
                });

        address.setDefault(true);
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(updatedAddress);
    }
    
    @Override
    @Cacheable(value = "defaultAddress", key = "#userId", cacheManager = "mediumCacheManager")
    public AddressResponse getDefaultAddress(Integer userId) {
        Address address = addressRepository.findDefaultByUserId(userId)
                .orElse(null);
        return address != null ? addressMapper.toAddressResponse(address) : null;
    }
}
