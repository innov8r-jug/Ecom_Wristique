package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.Profile;
import com.pranchal.ecommerce.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Validated
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    private ResponseEntity<String> checkSession(HttpSession session)
    {
        if(session.isNew())
        {
            return new ResponseEntity<>("Access Denied: Please login to proceed.", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    @GetMapping("/get")
    public ResponseEntity<?> getProfile(HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        Object emailId = session.getAttribute("email");
        if (emailId == null)
        {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }
        String email = emailId.toString();
        try
        {
            Profile profile = profileService.getProfile(email);
            return ResponseEntity.ok(profile);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("caught");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Profile profile, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if (sessionCheck != null) return sessionCheck;

        Object sessionEmailId = (String) session.getAttribute("email");
        if (sessionEmailId == null) {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }

        String sessionEmail = sessionEmailId.toString();
        if(!sessionEmail.equals(profile.getEmail()))
        {
            return new ResponseEntity<>("Cannot update profile for different user", HttpStatus.FORBIDDEN);
        }

        try
        {
            //email will not be updated as it is a primary key.
            Profile updatedProfile = profileService.updateProfile(profile);
            return ResponseEntity.ok(updatedProfile);
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/contact")
    public ResponseEntity<String> updateContactNumber(@RequestParam String contactNumber, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        Object emailId = (String) session.getAttribute("email");
        if(emailId == null)
        {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }

        String email = emailId.toString();

        try
        {
            profileService.updateContactNumber(email, contactNumber);
            return ResponseEntity.ok("Contact number updated successfully");
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/address")
    public ResponseEntity<String> updateAddress(@RequestParam String address, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        Object emailId = (String) session.getAttribute("email");
        if (emailId == null)
        {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }
        String email = emailId.toString();

        try
        {
            profileService.updateAddress(email, address);
            return ResponseEntity.ok("Address updated successfully");
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/dob")
    public ResponseEntity<String> updateDateOfBirth(@RequestParam String dateOfBirth, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        Object emailId = (String) session.getAttribute("email");
        if(emailId == null)
        {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }

        String email = emailId.toString();
        try
        {
            profileService.updateDateOfBirth(email, dateOfBirth);
            return ResponseEntity.ok("Date of birth updated successfully");
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<String> updatePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, HttpSession session) {

        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        Object emailId = session.getAttribute("email");
        if(emailId == null)
        {
            return new ResponseEntity<>("Email not found in session", HttpStatus.BAD_REQUEST);
        }
        String email = emailId.toString();

        try
        {

            profileService.updatePassword(email, currentPassword, newPassword, confirmPassword);
            return ResponseEntity.ok("Password updated successfully");
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}