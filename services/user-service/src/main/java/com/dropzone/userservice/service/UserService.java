package com.dropzone.userservice.service;

import com.dropzone.userservice.dto.CreateUserRequest;
import com.dropzone.userservice.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    UserDto createUser(CreateUserRequest request);
    UserDto updateUser(Long id, CreateUserRequest request);
}
