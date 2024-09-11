package com.hotel.army.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "guest")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Length(min = 4 , max = 70 , message = "اسم العميل لاي يجب ان يكون اكبر من 70 حرف او اصغر من 4 حروف")
    private String name;

    @NotNull
    @Length(min = 11 , max = 11 , message = "رقم هاتف العميل يجب ان يكون 11 رقما")
    private String mobileNumber;

    private String email;

    @NotNull
    @Length(min = 14 , max = 14 , message = "الرقم القومى يجب ان يكون 14 رقما")
    private String nationalId;

    private String maritalStatus;

    private int age;


    @Transient
    List<Session> sessions;

}