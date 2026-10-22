package com.smartvehicle.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mechanic_id")
    private User mechanic;

    @Column(nullable = false)
    private String serviceType;

    @Column(columnDefinition = "TEXT")
    private String customerSymptoms;

    @Column(columnDefinition = "TEXT")
    private String aiDiagnosisSummary;

    private LocalDate preferredDate;
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifecycleStage lifecycleStage = LifecycleStage.BOOKING;

    private boolean quotationApproved = false;

    @Column(columnDefinition = "TEXT")
    private String inspectionNotes;

    @Column(columnDefinition = "TEXT")
    private String serviceReport;

    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private BigDecimal labourHours = BigDecimal.ZERO;
    private BigDecimal labourRate = new BigDecimal("450.00");

    private Integer customerRating;
    private String customerFeedback;

    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsedPart> usedParts = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum LifecycleStage {
        BOOKING,
        VEHICLE_RECEIVED,
        INSPECTION,
        QUOTATION,
        APPROVAL,
        SERVICE_REPAIR,
        QUALITY_CHECK,
        PAYMENT,
        DELIVERY
    }
}
