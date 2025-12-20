package com.example.autoservice.controller;

import com.example.autoservice.model.Part;
import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.repository.PartRepository;
import com.example.autoservice.repository.ServiceOrderRepository;
import com.example.autoservice.repository.CustomerRepository;
import com.example.autoservice.repository.VehicleRepository;
import com.example.autoservice.repository.MechanicRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {

    private final ServiceOrderRepository repository;
    private final PartRepository partRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;

    public ServiceOrderController(ServiceOrderRepository repository,
                                  PartRepository partRepository,
                                  CustomerRepository customerRepository,
                                  VehicleRepository vehicleRepository,
                                  MechanicRepository mechanicRepository) {
        this.repository = repository;
        this.partRepository = partRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.mechanicRepository = mechanicRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC')")
    public List<ServiceOrder> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC', 'ROLE_CUSTOMER')")
    public ResponseEntity<ServiceOrder> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC')")
    @Transactional
    public ResponseEntity<?> create(@RequestBody ServiceOrder order) {
        if (order.getParts() != null) {
            for (Part p : order.getParts()) {
                Part dbPart = partRepository.findById(p.getId())
                        .orElseThrow(() -> new RuntimeException("Part not found: " + p.getId()));
                if (dbPart.getStock() <= 0) {
                    return ResponseEntity.badRequest().body("Part out of stock: " + dbPart.getName());
                }
                dbPart.setStock(dbPart.getStock() - 1);
                partRepository.save(dbPart);
            }
        }
        return ResponseEntity.ok(repository.save(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC')")
    public ResponseEntity<ServiceOrder> update(@PathVariable Long id, @RequestBody ServiceOrder updated) {
        return repository.findById(id).map(existing -> {
            existing.setDescription(updated.getDescription());
            existing.setLaborCost(updated.getLaborCost());
            existing.setCompletedTasks(updated.getCompletedTasks());
            existing.setRequiredTasks(updated.getRequiredTasks());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC')")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        return repository.findById(id).map(order -> {
            if (!order.canBeClosed()) {
                return ResponseEntity.badRequest().body("Not all tasks are completed");
            }
            order.setCompleted(true);
            repository.save(order);
            return ResponseEntity.ok(order);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ИСПРАВЛЕНО: Путь изменен на /completion-status, чтобы совпадало с вашим запросом
    @GetMapping("/{id}/completion-status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MECHANIC', 'ROLE_CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getOrderProgress(@PathVariable Long id) {
        return repository.findById(id).map(order -> {
            int total = order.getRequiredTasks() != null ? order.getRequiredTasks().size() : 0;
            int completed = order.getCompletedTasks() != null ? order.getCompletedTasks().size() : 0;
            double progress = total == 0 ? 0 : (double) completed / total * 100;

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", id);
            response.put("progressPercentage", progress);
            response.put("isFullyCompleted", order.isCompleted());
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_MECHANIC')")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        return repository.findById(id).map(order -> {
            if (order.isCompleted()) {
                return ResponseEntity.badRequest().body("Cannot cancel completed order");
            }
            if (order.getParts() != null) {
                for (Part p : order.getParts()) {
                    p.setStock(p.getStock() + 1);
                    partRepository.save(p);
                }
            }
            repository.delete(order);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repository.findById(id).map(order -> {
            if (order.getParts() != null) {
                for (Part p : order.getParts()) {
                    p.setStock(p.getStock() + 1);
                    partRepository.save(p);
                }
            }
            repository.delete(order);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}