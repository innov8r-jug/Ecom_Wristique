package com.pranchal.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class Profile {
    @Id
    private String email;

    private String about;
    private String dateOfBirth;
    private String contactNumber;
    private String gender;
    private String address;


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

}