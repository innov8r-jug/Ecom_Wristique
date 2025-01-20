package com.pranchal.ecommerce.service;

import com.pranchal.ecommerce.entity.Profile;
import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.repository.ProfileRepository;
import com.pranchal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;


@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String STATIC_SALT = "MyStaticSaltValue123!";

    public Profile getProfile(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        Profile profile = profileRepository.findByEmail(email);
        if (profile == null)
        {
            AppUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            profile = new Profile();
            profile.setEmail(user.getEmail());
            profile.setAbout("");
            profile.setAddress("");
            profile.setContactNumber("");
            profile.setDateOfBirth("");
            profile.setGender("");

            profile = profileRepository.save(profile);
        }
        return profile;
    }

    @Transactional
    public Profile updateProfile(Profile newProfile)
    {
        if (newProfile.getEmail() == null || newProfile.getEmail().trim().isEmpty())
        {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        Profile existingProfile = profileRepository.findByEmail(newProfile.getEmail());
        if (existingProfile == null) {
            AppUser user = userRepository.findByEmail(newProfile.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            existingProfile = new Profile();
            existingProfile.setEmail(newProfile.getEmail());
            existingProfile.setAbout("");
            existingProfile.setAddress("");
            existingProfile.setContactNumber("");
            existingProfile.setDateOfBirth("");
            existingProfile.setGender("");

            profileRepository.save(existingProfile);
        }

        validateProfileData(newProfile);

        updateProfileFields(existingProfile, newProfile);

        try
        {
            return profileRepository.save(existingProfile);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to save profile: " + e.getMessage());
        }
    }

    @Transactional
    public void updateContactNumber(String email, String newContactNumber)
    {
        Profile profile = getProfile(email); // Using existing getProfile method
        validateContactNumber(newContactNumber);
        profile.setContactNumber(newContactNumber.trim());
        profileRepository.save(profile);
    }

    @Transactional
    public void updateAddress(String email, String newAddress)
    {
        Profile profile = getProfile(email);
        if (newAddress == null || newAddress.trim().isEmpty())
        {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (newAddress.length() > 255)
        {
            throw new IllegalArgumentException("Address is too long");
        }
        profile.setAddress(newAddress.trim());
        profileRepository.save(profile);
    }

    @Transactional
    public void updateDateOfBirth(String email, String newDateOfBirth)
    {
        Profile profile = getProfile(email);
        validateDateOfBirth(newDateOfBirth);
        profile.setDateOfBirth(newDateOfBirth.trim());
        profileRepository.save(profile);
    }


    private void validateProfileData(Profile profile)
    {
        // Validate contact number
        if (profile.getContactNumber() != null && !profile.getContactNumber().trim().isEmpty())
        {
            validateContactNumber(profile.getContactNumber());
        }

        if (profile.getDateOfBirth() != null && !profile.getDateOfBirth().trim().isEmpty())
        {
            validateDateOfBirth(profile.getDateOfBirth());
        }

        if (profile.getGender() != null && !profile.getGender().trim().isEmpty())
        {
            validateGender(profile.getGender());
        }

        if (profile.getAddress() != null && profile.getAddress().length() > 255)
        {
            throw new IllegalArgumentException("Address is too long");
        }

        if (profile.getAbout() != null && profile.getAbout().length() > 1000)
        {
            throw new IllegalArgumentException("About section is too long");
        }
    }

    private void updateProfileFields(Profile existing, Profile newData)
    {
        if (newData.getAbout() != null) {
            existing.setAbout(newData.getAbout().trim());
        }
        if (newData.getDateOfBirth() != null) {
            existing.setDateOfBirth(newData.getDateOfBirth().trim());
        }
        if (newData.getContactNumber() != null) {
            existing.setContactNumber(newData.getContactNumber().trim());
        }
        if (newData.getGender() != null) {
            existing.setGender(newData.getGender().trim());
        }
        if (newData.getAddress() != null) {
            existing.setAddress(newData.getAddress().trim());
        }
    }

    private void validateContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().isEmpty())
        {
            throw new IllegalArgumentException("Contact number cannot be empty");
        }

        String digitsOnly = contactNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10)
        {
            throw new IllegalArgumentException("Contact number must be between 10 and 15 digits");
        }
    }

    private void validateDateOfBirth(String dateOfBirth)
    {
        if(dateOfBirth == null || dateOfBirth.trim().isEmpty())
        {
            throw new IllegalArgumentException("Date of birth cannot be empty");
        }

        try
        {
            LocalDate dob = LocalDate.parse(dateOfBirth);
            if(dob.isAfter(LocalDate.now()))
            {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }
        }
        catch(DateTimeParseException e)
        {
            throw new IllegalArgumentException("Date of birth must be in the format YYYY-MM-DD");
        }
    }

    private void validateGender(String gender)
    {
        if(gender == null || gender.trim().isEmpty())
        {
            throw new IllegalArgumentException("Gender cannot be empty");
        }
        String upperGender = gender.trim().toUpperCase();
        if(!upperGender.equals("MALE") && !upperGender.equals("FEMALE") && !upperGender.equals("OTHER"))
        {
            throw new IllegalArgumentException("Invalid gender value. Must be MALE, FEMALE, or OTHER");
        }
    }

    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword, String confirmPassword)
    {

        if(currentPassword == null || currentPassword.trim().isEmpty())
        {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        if(newPassword == null || newPassword.trim().isEmpty())
        {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        if(!newPassword.equals(confirmPassword))
        {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }


        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        String hashedCurrentPassword = hashPassword(currentPassword);
        if(!user.getPassword().equals(hashedCurrentPassword))
        {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String hashedNewPassword = hashPassword(newPassword);
        user.setPassword(hashedNewPassword);

        userRepository.save(user);
    }


    protected String hashPassword(String password)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(STATIC_SALT.getBytes());
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error hashing password", e);
        }
    }

}