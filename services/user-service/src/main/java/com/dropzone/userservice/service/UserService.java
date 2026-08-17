package com.dropzone.userservice.service;

import com.dropzone.userservice.dto.CreateUserRequest;
import com.dropzone.userservice.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    Page<UserDto> getAllUsersPaged(int page, int size);
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    UserDto createUser(CreateUserRequest request);
    UserDto updateUser(Long id, CreateUserRequest request);
}
