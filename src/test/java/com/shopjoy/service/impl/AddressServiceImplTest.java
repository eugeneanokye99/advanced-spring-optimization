package com.shopjoy.service.impl;

import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.entity.Address;
import com.shopjoy.entity.AddressType;
import com.shopjoy.entity.User;
import com.shopjoy.repository.AddressRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.dto.mapper.AddressMapperStruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapperStruct addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;
    private Address address;
    private CreateAddressRequest createAddressRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        address = new Address();
        address.setId(1);
        address.setUser(user);
        address.setStreetAddress("123 Main St");
        address.setDefault(false);

        createAddressRequest = CreateAddressRequest.builder()
                .userId(1)
                .addressType(AddressType.SHIPPING)
                .streetAddress("123 Main St")
                .city("City")
                .state("State")
                .postalCode("12345")
                .country("Country")
                .build();
    }

    @Test
    @DisplayName("Create Address - Success")
    void createAddress_Success() {
        when(addressMapper.toAddress(any(CreateAddressRequest.class))).thenReturn(address);
        when(userRepository.getReferenceById(1)).thenReturn(user);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toAddressResponse(any(Address.class))).thenReturn(new AddressResponse());

        AddressResponse response = addressService.createAddress(createAddressRequest);

        assertThat(response).isNotNull();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("Set Default Address - Success")
    void setDefaultAddress_Success() {
        Address currentDefault = new Address();
        currentDefault.setId(2);
        currentDefault.setUser(user);
        currentDefault.setDefault(true);

        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(addressRepository.findDefaultByUserId(1)).thenReturn(Optional.of(currentDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressMapper.toAddressResponse(any(Address.class))).thenReturn(new AddressResponse());

        AddressResponse response = addressService.setDefaultAddress(1);

        assertThat(response).isNotNull();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    @DisplayName("Delete Address - Success")
    void deleteAddress_Success() {
        when(addressRepository.existsById(1)).thenReturn(true);
        doNothing().when(addressRepository).deleteById(1);

        addressService.deleteAddress(1);

        verify(addressRepository).deleteById(1);
    }
}
