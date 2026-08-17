package com.dropzone.userservice.dto;

import com.dropzone.userservice.model.UserStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String keycloakUserId;
    private String name;
    private String email;
    private UserStatus status;
    private String preferences;
    private String purchaseHistoryRef;
}
