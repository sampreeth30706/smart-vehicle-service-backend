package com.smartvehicle.controller;

import com.smartvehicle.model.*;
import com.smartvehicle.repository.*;
import com.smartvehicle.security.JwtUtil;
import com.smartvehicle.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SmartVehicleRestController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final InventoryPartRepository inventoryPartRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final AiDiagnosticService aiDiagnosticService;
    private final NotificationSchedulerService notificationSchedulerService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // --- 1. Authentication: Register & Role-Validated Login ---
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String fullName = body.getOrDefault("fullName", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String phone = body.getOrDefault("phone", "").trim();
        String password = body.getOrDefault("password", "");
        String roleStr = body.getOrDefault("role", "CUSTOMER").toUpperCase();
        String specialization = body.getOrDefault("specialization", "");

        if (fullName.isEmpty() || email.isEmpty() || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full name, valid email, and password (min 6 chars) are required."));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "An account with this email is already registered."));
        }

        User.Role role = User.Role.valueOf(roleStr);
        User newUser = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone.isEmpty() ? "+91 90000 00000" : phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .specialization(specialization.isEmpty() ? role.name() : specialization)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(newUser);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());
        return ResponseEntity.status(201).body(Map.of("token", token, "user", saved));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        String requestedRole = body.getOrDefault("role", "").toUpperCase();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "No account found with this email. Please register first."));
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid password. Please check your credentials."));
        }
        if (!requestedRole.isEmpty() && !user.getRole().name().equals(requestedRole)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Role mismatch: This account is registered as " + user.getRole().name() + ", not " + requestedRole + "."
            ));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- 2. Vehicle Management ---
    @GetMapping("/vehicles")
    public List<Vehicle> getVehicles(@RequestParam(required = false) Long ownerId) {
        if (ownerId != null) {
            return vehicleRepository.findByOwnerId(ownerId);
        }
        return vehicleRepository.findAll();
    }

    @PostMapping("/vehicles")
    public Vehicle createVehicle(@RequestBody Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    // --- 3. Booking & 9-Stage Service Lifecycle ---
    @GetMapping("/services")
    public List<ServiceRequest> getServiceRequests(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long mechanicId) {
        if (customerId != null) return serviceRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (mechanicId != null) return serviceRequestRepository.findByMechanicIdOrderByCreatedAtDesc(mechanicId);
        return serviceRequestRepository.findAll();
    }

    @PostMapping("/services")
    public ServiceRequest createBooking(@RequestBody ServiceRequest request) {
        request.setBookingCode("SRV-" + (1000 + new Random().nextInt(9000)));
        request.setLifecycleStage(ServiceRequest.LifecycleStage.BOOKING);
        request.setCreatedAt(LocalDateTime.now());
        return serviceRequestRepository.save(request);
    }

    @PatchMapping("/services/{id}/stage")
    public ResponseEntity<ServiceRequest> updateLifecycleStage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return serviceRequestRepository.findById(id).map(sr -> {
            if (updates.containsKey("lifecycleStage")) {
                sr.setLifecycleStage(ServiceRequest.LifecycleStage.valueOf(updates.get("lifecycleStage").toString()));
            }
            if (updates.containsKey("inspectionNotes")) {
                sr.setInspectionNotes(updates.get("inspectionNotes").toString());
            }
            if (updates.containsKey("serviceReport")) {
                sr.setServiceReport(updates.get("serviceReport").toString());
            }
            if (updates.containsKey("quotationApproved")) {
                sr.setQuotationApproved(Boolean.parseBoolean(updates.get("quotationApproved").toString()));
            }
            if (updates.containsKey("labourHours")) {
                sr.setLabourHours(new BigDecimal(updates.get("labourHours").toString()));
            }
            sr.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(serviceRequestRepository.save(sr));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- 4. Inventory, Billing & Invoices ---
    @GetMapping("/inventory")
    public List<InventoryPart> getInventory() {
        return inventoryPartRepository.findAll();
    }

    @PostMapping("/inventory")
    public InventoryPart saveInventoryPart(@RequestBody InventoryPart part) {
        return inventoryPartRepository.save(part);
    }

    @GetMapping("/invoices")
    public List<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    // --- 5. AI Intelligence Layer ---
    @PostMapping("/ai/analyze")
    public Map<String, Object> analyzeIssue(@RequestBody Map<String, Object> payload) {
        String make = payload.getOrDefault("make", "Car").toString();
        String model = payload.getOrDefault("model", "Model").toString();
        int mileage = Integer.parseInt(payload.getOrDefault("mileage", "25000").toString());
        String symptoms = payload.getOrDefault("symptoms", "").toString();
        return aiDiagnosticService.analyzeVehicleIssue(make, model, mileage, symptoms);
    }

    // --- 6. Notifications & Scheduler Trigger ---
    @GetMapping("/notifications")
    public List<NotificationLog> getNotifications() {
        return notificationLogRepository.findAll();
    }

    @PostMapping("/notifications/scan")
    public Map<String, Object> triggerExpiryAndStockScan() {
        int count = notificationSchedulerService.runDailyComplianceAndStockScan();
        return Map.of("alertsGenerated", count, "timestamp", LocalDateTime.now());
    }
}
