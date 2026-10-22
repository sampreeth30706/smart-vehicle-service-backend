package com.smartvehicle.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_parts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String partName;

    @Column(nullable = false)
    private String category;

    private int stockQuantity;
    private int reorderLevel;
    private BigDecimal unitPrice;
    private String supplier;
}
