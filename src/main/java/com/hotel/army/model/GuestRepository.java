package com.hotel.army.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest findByNationalId(String nationalId);
}