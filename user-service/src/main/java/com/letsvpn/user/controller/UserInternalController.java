package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.common.data.entity.User;
import com.letsvpn.user.dto.UserInitializationRequest;
import com.letsvpn.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "User Internal Management API", description = "Internal APIs for user management, typically called by other services")
@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
@Slf4j // Added
public class UserInternalController {

    @Autowired
    private UserService userService;


    @Operation(summary = "Initialize new user and assign free nodes",
            description = "Called by auth-service after user registration to create user profile in user-service and assign default/free resources.")
    @PostMapping("/create")
    public R<Void> initializeNewUser(@Valid @RequestBody UserInitializationRequest request) {
        log.info("Received internal request to initialize user: {}", request.getUsername());
        try {
            userService.initializeNewUser(request.getUserId(), request.getUsername());

            log.info("Successfully initialized user: {}", request.getUsername());
            return R.success();
        } catch (Exception e) {
            log.error("Error initializing new user (userId: {}): {}", request.getUserId(), e.getMessage(), e);
            // R.fail() will be handled by GlobalExceptionHandler to return a proper error response
            // No need to return R.fail() directly if GlobalExceptionHandler is set up
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}
