package com.pranchal.ecommerce.service;

import com.pranchal.ecommerce.entity.AppUser;
import com.pranchal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final String STATIC_SALT = "MyStaticSaltValue123!";

    public void registerUser(AppUser appUser) {
        Optional<AppUser> existingUser = userRepository.findByEmail(appUser.getEmail());

        String emailRegex = "^[A-Za-z0-9.]+@gmail\\.com$";
        if(!appUser.getEmail().matches(emailRegex))
        {
            throw new IllegalArgumentException("Invalid email format. Only letters (A-Z, a-z) and digits (1-9) are allowed before '@gmail.com'.");
        }

        if(existingUser.isPresent())
        {
            throw new IllegalArgumentException("ID with this email already exists");
        }

        String hashedPassword = hashPassword(appUser.getPassword());
        appUser.setPassword(hashedPassword);
        appUser.setActive();
        userRepository.save(appUser);
    }

    public boolean checkCredentials(String email, String password)
    {
        AppUser user = getUserByEmail(email);
        if(user == null)
        {
            return false;
        }

        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(user.getPassword());
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

    public AppUser getUserByEmail(String email)
    {
        return userRepository.findByEmail(email).orElse(null);
    }

}