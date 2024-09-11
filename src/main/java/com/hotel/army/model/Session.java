package com.hotel.army.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "room_id")
    private Room room;

    private String startedAt;
    private String endedAt;
    private String details;
    private String saves;
    private Long dueAmount;
    private Long inAdvanceAmount;
    private Long remainsAmount;
    private Long discountAmount;
    private Long returnedAmount;
    private Long paidAmount;
    private boolean isEnded;
}