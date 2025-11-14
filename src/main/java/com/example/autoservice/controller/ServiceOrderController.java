package com.example.autoservice.controller;

import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {

    private final ServiceOrderRepository repository;

    public ServiceOrderController(ServiceOrderRepository repository) {
        this.repository = repository;
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
}
