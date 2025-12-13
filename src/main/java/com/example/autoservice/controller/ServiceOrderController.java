package com.example.autoservice.controller;

import com.example.autoservice.model.Part;
import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.repository.PartRepository;
import com.example.autoservice.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {

    private final ServiceOrderRepository repository;
    private final PartRepository partRepository;

    public ServiceOrderController(ServiceOrderRepository repository, PartRepository partRepository) {
        this.repository = repository;
        this.partRepository = partRepository;
    }

    @GetMapping
    public List<ServiceOrder> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrder> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Бизнес-операция: получение общей стоимости заказа
    @GetMapping("/{orderId}/total-cost")
    public ResponseEntity<Double> getTotalCost(@PathVariable Long orderId) {
        return repository.findById(orderId)
                .map(order -> ResponseEntity.ok(order.getTotalCost()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ServiceOrder> create(@RequestBody ServiceOrder order) {
        if (order.getParts() != null && !order.getParts().isEmpty()) {
            for (Part part : order.getParts()) {
                Part managedPart = partRepository.findById(part.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Part not found: " + part.getId()));
                managedPart.decreaseStock(1);
                partRepository.save(managedPart);
            }
        }
        return ResponseEntity.ok(repository.save(order));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ServiceOrder> update(@PathVariable Long id, @RequestBody ServiceOrder updated) {
        return repository.findById(id).map(existing -> {
            // Старые ID деталей
            Set<Long> oldPartIds = existing.getParts() == null ?
                    Collections.emptySet() :
                    existing.getParts().stream()
                            .map(Part::getId)
                            .collect(Collectors.toSet());

            // Новые ID деталей
            Set<Long> newPartIds = updated.getParts() == null ?
                    Collections.emptySet() :
                    updated.getParts().stream()
                            .map(Part::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

            // Возврат на склад удалённых деталей
            if (existing.getParts() != null) {
                existing.getParts().stream()
                        .filter(p -> !newPartIds.contains(p.getId()))
                        .forEach(part -> {
                            partRepository.findById(part.getId()).ifPresent(managed -> {
                                managed.increaseStock(1);
                                partRepository.save(managed);
                            });
                        });
            }

            // Списание новых деталей
            if (updated.getParts() != null) {
                updated.getParts().stream()
                        .filter(p -> !oldPartIds.contains(p.getId()))
                        .forEach(part -> {
                            Part managed = partRepository.findById(part.getId())
                                    .orElseThrow(() -> new IllegalArgumentException("Part not found: " + part.getId()));
                            managed.decreaseStock(1);
                            partRepository.save(managed);
                        });
            }

            // Обновление полей заказа
            existing.setCustomer(updated.getCustomer());
            existing.setVehicle(updated.getVehicle());
            existing.setMechanic(updated.getMechanic());
            existing.setParts(updated.getParts());
            existing.setRequiredTasks(updated.getRequiredTasks());
            existing.setCompletedTasks(updated.getCompletedTasks());
            existing.setLaborCost(updated.getLaborCost());
            existing.setDescription(updated.getDescription());
            existing.setCompleted(updated.isCompleted());

            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<ServiceOrder> orderOpt = repository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();

        // Возврат всех деталей на склад
        if (order.getParts() != null) {
            for (Part part : order.getParts()) {
                partRepository.findById(part.getId()).ifPresent(managed -> {
                    managed.increaseStock(1);
                    partRepository.save(managed);
                });
            }
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build(); // Правильный тип: ResponseEntity<Void>
    }

    @GetMapping("/{orderId}/completion-status")
    public ResponseEntity<String> getCompletionStatus(@PathVariable Long orderId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();
        if (order.isCompleted()) {
            return ResponseEntity.ok("COMPLETED");
        }

        int total = order.getRequiredTasks() != null ? order.getRequiredTasks().size() : 0;
        int completed = order.getCompletedTasks() != null ? order.getCompletedTasks().size() : 0;
        double percent = total > 0 ? (completed * 100.0) / total : 0;

        return ResponseEntity.ok(String.format("IN_PROGRESS - %.1f%% complete (%d/%d tasks)", percent, completed, total));
    }

    @PutMapping("/{orderId}/cancel")
    @Transactional
    public ResponseEntity<ServiceOrder> cancelOrder(@PathVariable Long orderId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();
        if (order.isCompleted()) {
            return ResponseEntity.badRequest().body(order);
        }

        // Возврат всех деталей на склад
        if (order.getParts() != null) {
            for (Part part : order.getParts()) {
                partRepository.findById(part.getId()).ifPresent(managed -> {
                    managed.increaseStock(1);
                    partRepository.save(managed);
                });
            }
        }

        order.getParts().clear();
        order.setCompleted(false);

        return ResponseEntity.ok(repository.save(order));
    }
}