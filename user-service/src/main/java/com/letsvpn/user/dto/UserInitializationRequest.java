package com.letsvpn.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInitializationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "User ID cannot be empty")
    private String userId;

    @NotEmpty(message = "Username cannot be empty")
    private String username;

    // Add other fields if needed, like email
}