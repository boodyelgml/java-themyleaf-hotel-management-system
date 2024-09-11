package com.hotel.army;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGuestRequest {
    private String name;
    private String mobileNumber;
    private String email;
    private String nationalId;
    private String maritalStatus;
    private int age;
}
