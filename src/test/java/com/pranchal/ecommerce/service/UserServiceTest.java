package com.pranchal.ecommerce.service;

import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testRegisterUser_Success() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword("password123");

        when(userRepository.findByEmail(appUser.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.registerUser(appUser));
        verify(userRepository, times(1)).save(appUser);
        assertNotNull(appUser.getPassword()); // Ensures password is hashed
    }

    @Test
    void testRegisterUser_InvalidEmail() {
        AppUser appUser = new AppUser();
        appUser.setEmail("invalid-email.com");
        appUser.setPassword("password123");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(appUser));
        assertEquals("Invalid email format. Only letters (A-Z, a-z) and digits (1-9) are allowed before '@example.com'.", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword("password123");

        when(userRepository.findByEmail(appUser.getEmail())).thenReturn(Optional.of(appUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(appUser));
        assertEquals("ID with this email already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void testCheckCredentials_Success() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword(userService.hashPassword("password123"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(appUser));

        boolean isValid = userService.checkCredentials("test@example.com", "password123");
        assertTrue(isValid);
    }

    @Test
    void testCheckCredentials_InvalidPassword() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword(userService.hashPassword("password123"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(appUser));

        boolean isValid = userService.checkCredentials("test@example.com", "wrongpassword");
        assertFalse(isValid);
    }

    @Test
    void testCheckCredentials_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        boolean isValid = userService.checkCredentials("unknown@example.com", "password123");
        assertFalse(isValid);
    }
}
