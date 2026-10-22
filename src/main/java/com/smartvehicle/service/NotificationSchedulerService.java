package com.smartvehicle.service;

import com.smartvehicle.model.*;
import com.smartvehicle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final VehicleRepository vehicleRepository;
    private final InventoryPartRepository inventoryRepository;
    private final NotificationLogRepository notificationRepository;

    /**
     * Runs daily (or on-demand via Admin API) to scan for:
     * 1. Upcoming or expired Insurance policies (within 30 days)
     * 2. Upcoming or expired PUC certificates (within 15 days)
     * 3. Low spare part stock in inventory
     */
    @Scheduled(cron = "${app.scheduler.expiry-check-cron}")
    public int runDailyComplianceAndStockScan() {
        int generated = 0;
        LocalDate today = LocalDate.now();

        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle v : vehicles) {
            if (v.getInsuranceExpiry() != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, v.getInsuranceExpiry());
                if (daysLeft <= 30) {
                    notificationRepository.save(NotificationLog.builder()
                            .recipientUserId(v.getOwner().getId())
                            .channel("EMAIL")
                            .alertType("INSURANCE_EXPIRY")
                            .title("Insurance Expiry Alert: " + v.getRegistrationNumber())
                            .message("Insurance policy for " + v.getMake() + " " + v.getModel() + " (" + v.getRegistrationNumber() + ") expires in " + daysLeft + " days.")
                            .build());
                    generated++;
                }
            }
            if (v.getPucExpiry() != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, v.getPucExpiry());
                if (daysLeft <= 15) {
                    notificationRepository.save(NotificationLog.builder()
                            .recipientUserId(v.getOwner().getId())
                            .channel("SMS")
                            .alertType("PUC_EXPIRY")
                            .title("PUC Certificate Alert: " + v.getRegistrationNumber())
                            .message("PUC certificate for " + v.getRegistrationNumber() + " expires in " + daysLeft + " days. Book an emission test soon.")
                            .build());
                    generated++;
                }
            }
        }

        List<InventoryPart> parts = inventoryRepository.findAll();
        for (InventoryPart part : parts) {
            if (part.getStockQuantity() <= part.getReorderLevel()) {
                notificationRepository.save(NotificationLog.builder()
                        .recipientUserId(1L)
                        .channel("SYSTEM")
                        .alertType("LOW_STOCK")
                        .title("Low Inventory Stock: " + part.getPartName())
                        .message("SKU " + part.getSku() + " has only " + part.getStockQuantity() + " units remaining (Reorder threshold: " + part.getReorderLevel() + ").")
                        .build());
                generated++;
            }
        }
        return generated;
    }
}
