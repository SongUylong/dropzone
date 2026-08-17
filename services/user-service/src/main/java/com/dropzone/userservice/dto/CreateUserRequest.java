package com.dropzone.userservice.dto;

import com.dropzone.userservice.model.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String keycloakUserId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    private UserStatus status;
    private String preferences;
    private String purchaseHistoryRef;
}
