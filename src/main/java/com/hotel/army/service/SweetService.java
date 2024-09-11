package com.hotel.army.service;

import com.hotel.army.model.Sweet;
import com.hotel.army.model.SweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SweetService {
    private final SweetRepository sweetRepository;


    public List<Sweet> getAll() {
        return this.sweetRepository.findAll();
    }

}
