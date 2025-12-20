package com.example.autoservice.controller;

import com.example.autoservice.model.Mechanic;
import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.repository.MechanicRepository;
import com.example.autoservice.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    private final MechanicRepository repo;
    private final ServiceOrderRepository serviceOrderRepository;

    public MechanicController(MechanicRepository repo, ServiceOrderRepository serviceOrderRepository) {
        this.repo = repo;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @GetMapping
    public List<Mechanic> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Mechanic> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mechanic> create(@RequestBody Mechanic m) {
        Mechanic saved = repo.save(m);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mechanic> update(@PathVariable Long id, @RequestBody Mechanic updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setSpecialization(updated.getSpecialization());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Бизнес-операция: Получение текущей загрузки механика (количество активных заказов)
    @GetMapping("/{id}/workload")
    public ResponseEntity<Map<String, Object>> getMechanicWorkload(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByMechanicId(id);

        long activeOrdersCount = orders.stream()
                .filter(order -> !order.isCompleted())
                .count();

        return ResponseEntity.ok(Map.of(
                "mechanicId", id,
                "activeOrders", activeOrdersCount,
                "status", activeOrdersCount > 2 ? "BUSY" : "AVAILABLE"
        ));
    }
}