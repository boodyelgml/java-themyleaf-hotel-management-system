package com.hotel.army.service;

import com.hotel.army.model.Guest;
import com.hotel.army.model.GuestRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {
    private final GuestRepository guestRepository;


    public List<Guest> getAll() {
        return this.guestRepository.findAll();
    }

    public Guest findOneWithNationalId(String nationalId) {
        return this.guestRepository.findByNationalId(nationalId);
    }

    public Guest save(Guest guest) {
        return this.guestRepository.saveAndFlush(guest);
    }

    public Guest createGuest(@Valid Guest guest) {
        Guest model = findOneWithNationalId(guest.getNationalId());
        if (model == null) {
            return this.guestRepository.saveAndFlush(guest);
        } else {
            throw new RuntimeException("Guest with the provided National ID already exists");
        }
    }

}
