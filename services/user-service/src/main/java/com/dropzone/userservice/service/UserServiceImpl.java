package com.dropzone.userservice.service;

import com.dropzone.userservice.dto.CreateUserRequest;
import com.dropzone.userservice.dto.UserDto;
import com.dropzone.userservice.event.UserEvent;
import com.dropzone.userservice.event.UserEventProducer;
import com.dropzone.userservice.model.User;
import com.dropzone.userservice.model.UserStatus;
import com.dropzone.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return UserDto.fromEntity(user);
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        User user = User.builder()
                .keycloakUserId(request.getKeycloakUserId())
                .name(request.getName())
                .email(request.getEmail())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .preferences(request.getPreferences())
                .purchaseHistoryRef(request.getPurchaseHistoryRef())
                .build();
        User savedUser = userRepository.save(user);

        userEventProducer.sendUserEvent(UserEvent.builder()
                .eventType("UserRegistered")
                .userId(savedUser.getId())
                .keycloakUserId(savedUser.getKeycloakUserId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .timestamp(Instant.now())
                .build());

        return UserDto.fromEntity(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        if (request.getPreferences() != null) user.setPreferences(request.getPreferences());
        if (request.getPurchaseHistoryRef() != null) user.setPurchaseHistoryRef(request.getPurchaseHistoryRef());
        if (request.getKeycloakUserId() != null) user.setKeycloakUserId(request.getKeycloakUserId());

        User updatedUser = userRepository.save(user);
        return UserDto.fromEntity(updatedUser);
    }
}
