package com.smartvehicle.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    private int manufacturingYear;
    private String fuelType;
    private String vin;
    private int currentMileage;
    private int lastServiceMileage;
    private LocalDate lastServiceDate;
    private String insurancePolicyNo;
    private LocalDate insuranceExpiry;
    private String pucCertificateNo;
    private LocalDate pucExpiry;
}
