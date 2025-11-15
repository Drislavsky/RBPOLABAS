package com.example.autoservice.controller;

import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.model.Part;
import com.example.autoservice.repository.ServiceOrderRepository;
import com.example.autoservice.repository.PartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PostMapping
    public ServiceOrder create(@RequestBody ServiceOrder order) {
        return repository.save(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrder> update(@PathVariable Long id, @RequestBody ServiceOrder updated) {
        Optional<ServiceOrder> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder existing = optional.get();
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
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ServiceOrder> closeOrder(@PathVariable Long id) {
        Optional<ServiceOrder> optionalOrder = repository.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = optionalOrder.get();

        if (!order.canBeClosed()) {
            return ResponseEntity.badRequest().build();
        }

        order.setCompleted(true);
        repository.save(order);
        return ResponseEntity.ok(order);
    }

    // Business Operation 1: Add part to service order
    @PostMapping("/{orderId}/parts/{partId}")
    @Transactional
    public ResponseEntity<ServiceOrder> addPartToOrder(@PathVariable Long orderId, @PathVariable Long partId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        Optional<Part> partOpt = partRepository.findById(partId);

        if (orderOpt.isEmpty() || partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();
        Part part = partOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        if (part.getStock() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Decrease part stock
        part.setStock(part.getStock() - 1);
        partRepository.save(part);

        // Add part to order
        if (!order.getParts().contains(part)) {
            order.getParts().add(part);
        }

        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // Business Operation 2: Complete a task in service order
    @PostMapping("/{orderId}/complete-task")
    public ResponseEntity<ServiceOrder> completeTask(@PathVariable Long orderId, @RequestBody String task) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if task exists in required tasks
        if (!order.getRequiredTasks().contains(task)) {
            return ResponseEntity.badRequest().build();
        }

        // Check if task is already completed
        if (order.getCompletedTasks().contains(task)) {
            return ResponseEntity.badRequest().build();
        }

        // Add task to completed tasks
        order.getCompletedTasks().add(task);

        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // Business Operation 3: Get total cost of service order
    @GetMapping("/{orderId}/total-cost")
    public ResponseEntity<Double> getTotalCost(@PathVariable Long orderId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();
        double totalCost = order.getTotalCost();

        return ResponseEntity.ok(totalCost);
    }

    // Business Operation 4: Add required task to service order
    @PostMapping("/{orderId}/required-tasks")
    public ResponseEntity<ServiceOrder> addRequiredTask(@PathVariable Long orderId, @RequestBody String task) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        // Add task to required tasks if not already present
        if (!order.getRequiredTasks().contains(task)) {
            order.getRequiredTasks().add(task);
        }

        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // Business Operation 5: Get all active (incomplete) service orders
    @GetMapping("/active")
    public List<ServiceOrder> getActiveOrders() {
        return repository.findAll().stream()
                .filter(order -> !order.isCompleted())
                .toList();
    }

    // Business Operation 6: Get service orders by customer
    @GetMapping("/customer/{customerId}")
    public List<ServiceOrder> getOrdersByCustomer(@PathVariable Long customerId) {
        return repository.findAll().stream()
                .filter(order -> order.getCustomer() != null && order.getCustomer().getId().equals(customerId))
                .toList();
    }

    // Business Operation 7: Update labor cost
    @PatchMapping("/{orderId}/labor-cost")
    public ResponseEntity<ServiceOrder> updateLaborCost(@PathVariable Long orderId, @RequestParam Double laborCost) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        if (laborCost < 0) {
            return ResponseEntity.badRequest().build();
        }

        order.setLaborCost(laborCost);
        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // Business Operation 8: Remove part from service order
    @DeleteMapping("/{orderId}/parts/{partId}")
    @Transactional
    public ResponseEntity<ServiceOrder> removePartFromOrder(@PathVariable Long orderId, @PathVariable Long partId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        Optional<Part> partOpt = partRepository.findById(partId);

        if (orderOpt.isEmpty() || partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();
        Part part = partOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        // Remove part from order
        order.getParts().remove(part);

        // Increase part stock
        part.setStock(part.getStock() + 1);
        partRepository.save(part);

        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    // Business Operation 9: Get service order completion status
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

        int totalTasks = order.getRequiredTasks().size();
        int completedTasks = order.getCompletedTasks().size();
        double completionPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;

        String status = String.format("IN_PROGRESS - %.1f%% complete (%d/%d tasks)",
                completionPercentage, completedTasks, totalTasks);

        return ResponseEntity.ok(status);
    }

    // Business Operation 10: Cancel service order
    @PutMapping("/{orderId}/cancel")
    @Transactional
    public ResponseEntity<ServiceOrder> cancelOrder(@PathVariable Long orderId) {
        Optional<ServiceOrder> orderOpt = repository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceOrder order = orderOpt.get();

        if (order.isCompleted()) {
            return ResponseEntity.badRequest().build();
        }

        // Return parts to stock
        for (Part part : order.getParts()) {
            part.setStock(part.getStock() + 1);
            partRepository.save(part);
        }

        // Clear parts from order
        order.getParts().clear();
        order.setCompleted(false);

        ServiceOrder savedOrder = repository.save(order);
        return ResponseEntity.ok(savedOrder);
    }
}