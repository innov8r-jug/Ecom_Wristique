package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class AppUserController
{

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AppUser appUser)
    {

        System.out.println("Received JSON data: " + appUser.getName());
        System.out.println("Email: " + appUser.getEmail());
        System.out.println("Role: " + appUser.getRole());
        System.out.println("IsActive: " + appUser.isActive());

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
    public ResponseEntity<String> loginUser(@RequestParam String email, @RequestParam String password, HttpSession session)
    {
        boolean isLoginSuccessful = userService.checkCredentials(email, password);
        if(isLoginSuccessful)
        {
            AppUser user = userService.getUserByEmail(email);
            session.setAttribute("name", user.getName());
            session.setAttribute("role", user.getRole());
            session.setAttribute("email", user.getEmail());
            return new ResponseEntity<>("User Login Successful. Session created.", HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>("Login Failed", HttpStatus.UNAUTHORIZED);
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
        String role;

        if(roleObj instanceof String)
        {
            role = (String) roleObj;
        }
        else if(roleObj != null)
        {

            role = roleObj.toString();
        }
        else
        {
            role = null;
        }

        sessionInfo.put("role", role);
        return ResponseEntity.ok(sessionInfo);
    }
}
