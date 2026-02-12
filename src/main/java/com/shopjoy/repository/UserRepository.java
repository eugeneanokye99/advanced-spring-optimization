package com.shopjoy.repository;

import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByUserType(UserType userType);
}
