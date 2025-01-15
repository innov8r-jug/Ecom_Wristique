package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.entity.Role;
import com.pranchal.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserControllerTest
{

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AppUserController appUserController;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success()
    {
        AppUser user = new AppUser();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setActive();

        ResponseEntity<String> response = appUserController.registerUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void registerUser_Failure()
    {

        AppUser user = new AppUser();
        doThrow(new IllegalArgumentException("Email already exists"))
                .when(userService).registerUser(user);

        // Act
        ResponseEntity<String> response = appUserController.registerUser(user);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already exists", response.getBody());
    }

    @Test
    void loginUser_Success()
    {
        String email = "test@example.com";
        String password = "password123";
        AppUser user = new AppUser();
        user.setName("Test User");
        user.setEmail(email);
        user.setRole(Role.USER);

        when(userService.checkCredentials(email, password)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(user);

        ResponseEntity<String> response = appUserController.loginUser(email, password, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User Login Successful. Session created.", response.getBody());
        verify(session).setAttribute("name", user.getName());
        verify(session).setAttribute("role", user.getRole());
        verify(session).setAttribute("email", user.getEmail());
    }

    @Test
    void loginUser_Failure()
    {
        String email = "test@example.com";
        String password = "wrongpassword";
        when(userService.checkCredentials(email, password)).thenReturn(false);

        ResponseEntity<String> response = appUserController.loginUser(email, password, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Login Failed", response.getBody());
    }

    @Test
    void logoutUser_Success()
    {
        ResponseEntity<String> response = appUserController.logoutUser(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User logged out and session invalidated.", response.getBody());
        verify(session).invalidate();
    }

    @Test
    void getSessionInfo_ValidSession()
    {
        when(session.isNew()).thenReturn(false);
        when(session.getAttribute("role")).thenReturn(Role.USER);

        ResponseEntity<Map<String, String>> response = appUserController.getSessionInfo(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Role.USER.toString(), response.getBody().get("role"));
    }

    @Test
    void getSessionInfo_InvalidSession()
    {
        when(session.isNew()).thenReturn(true);

        ResponseEntity<Map<String, String>> response = appUserController.getSessionInfo(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}