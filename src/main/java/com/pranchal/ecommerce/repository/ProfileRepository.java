package com.pranchal.ecommerce.repository;

import com.pranchal.ecommerce.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Profile findByEmail(String email);
}