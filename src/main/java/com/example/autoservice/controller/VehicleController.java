package com.example.autoservice.controller;

import com.example.autoservice.model.Customer;
import com.example.autoservice.model.Vehicle;
import com.example.autoservice.repository.CustomerRepository;
import com.example.autoservice.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository repo;
    private final CustomerRepository customerRepository;

    public VehicleController(VehicleRepository repo, CustomerRepository customerRepository) {
        this.repo = repo;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<Vehicle> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle v) {
        Vehicle saved = repo.save(v);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle updated) {
        return repo.findById(id).map(existing -> {
            existing.setBrand(updated.getBrand());
            existing.setModel(updated.getModel());
            existing.setYear(updated.getYear());
            existing.setVin(updated.getVin());
            existing.setOwner(updated.getOwner());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Бизнес-операция: Передача права собственности (смена владельца авто)
    @PostMapping("/{id}/transfer-ownership")
    public ResponseEntity<Vehicle> transferOwnership(@PathVariable Long id, @RequestParam Long newOwnerId) {
        Optional<Vehicle> vehicleOpt = repo.findById(id);
        Optional<Customer> newOwnerOpt = customerRepository.findById(newOwnerId);

        if (vehicleOpt.isEmpty() || newOwnerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = vehicleOpt.get();
        Customer newOwner = newOwnerOpt.get();

        // Логика передачи: меняем владельца и обновляем запись
        vehicle.setOwner(newOwner);
        // Можно добавить логику записи в историю, если бы была такая сущность

        Vehicle saved = repo.save(vehicle);
        return ResponseEntity.ok(saved);
    }
}