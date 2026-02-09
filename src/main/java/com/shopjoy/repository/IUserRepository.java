package com.shopjoy.repository;

import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends GenericRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> authenticate(String username, String password);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    List<User> findByUserType(UserType userType);
    void changePassword(int userId, String newPassword);
}
