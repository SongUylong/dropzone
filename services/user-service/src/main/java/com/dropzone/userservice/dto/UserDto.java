package com.dropzone.userservice.dto;

import com.dropzone.userservice.model.User;
import com.dropzone.userservice.model.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String keycloakUserId;
    private String name;
    private String email;
    private UserStatus status;
    private String preferences;
    private String purchaseHistoryRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .keycloakUserId(user.getKeycloakUserId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .preferences(user.getPreferences())
                .purchaseHistoryRef(user.getPurchaseHistoryRef())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
