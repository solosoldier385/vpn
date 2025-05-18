package com.letsvpn.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationInternalRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    // Add any other fields user-service might need for initialization,
    // e.g., email if user-service also stores it.
}