package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.repository.ProductRepository;
import com.pranchal.ecommerce.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class AppUserController {

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AppUser appUser)
    {
        try
        {
            userService.registerUser(appUser);
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        }
        catch(IllegalArgumentException ex)
        {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String email, @RequestParam String password, HttpSession session) {
        boolean isLoginSuccessful = userService.checkCredentials(email, password);
        if (isLoginSuccessful) {
            AppUser user = userService.getUserByEmail(email);
            if (user.isTwoFactorEnabled()) {
                try {
                    Map<String, Object> otpResponse = twoFactorService.generateOTP(user.getEmail());
                    session.setAttribute("txId", otpResponse.get("txId"));
                    session.setAttribute("pendingEmail", email);
                    return ResponseEntity.ok().body(Map.of(
                            "requiresOTP", true,
                            "message", "OTP has been sent to your email"
                    ));
                } catch (Exception e) {
                    return new ResponseEntity<>("Error generating OTP", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            // Complete login if 2FA is not enabled (shouldn't happen as it's enabled by default)
            completeLogin(user, session);
            return ResponseEntity.ok().body(Map.of(
                    "requiresOTP", false,
                    "message", "Login successful"
            ));
        }
        return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
    private void completeLogin(AppUser user, HttpSession session) {
        session.setAttribute("name", user.getName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("email", user.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request, HttpSession session) {
        String otp = request.get("otp");
        String txId = (String) session.getAttribute("txId");
        String pendingEmail = (String) session.getAttribute("pendingEmail");

        if (txId == null || pendingEmail == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "No pending OTP verification"));
        }

        try {
            Map<String, Object> response = twoFactorService.validateOTP(txId, otp);
            if ("SUCCESS".equals(response.get("status"))) {
                AppUser user = userService.getUserByEmail(pendingEmail);
                completeLogin(user, session);
                session.removeAttribute("txId");
                session.removeAttribute("pendingEmail");
                return ResponseEntity.ok()
                        .body(Map.of("message", "Login successful"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid OTP"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error validating OTP"));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpSession session)
    {
        session.invalidate();
        return new ResponseEntity<>("User logged out and session invalidated.", HttpStatus.OK);
    }

    @GetMapping("/session-info")
    public ResponseEntity<Map<String, String>> getSessionInfo(HttpSession session)
    {
        if(session.isNew())
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, String> sessionInfo = new HashMap<>();
        Object roleObj = session.getAttribute("role");
        String role = (roleObj instanceof String) ? (String) roleObj :
                (roleObj != null) ? roleObj.toString() : null;
        sessionInfo.put("role", role);
        return ResponseEntity.ok(sessionInfo);
    }
}