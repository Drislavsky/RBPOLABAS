package com.example.autoservice.controller;

import com.example.autoservice.model.Part;
import com.example.autoservice.repository.PartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final PartRepository repository;

    public PartController(PartRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Part> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Part> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Part> create(@RequestBody Part part) {
        part.setIsAvailable(part.getStock() > 0);
        Part saved = repository.save(part);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Part> update(@PathVariable Long id, @RequestBody Part updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            existing.setDescription(updated.getDescription());
            existing.setCategory(updated.getCategory());
            existing.setManufacturer(updated.getManufacturer());
            existing.setPartNumber(updated.getPartNumber());
            existing.setIsAvailable(updated.getStock() > 0);
            repository.save(existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Бизнес-операция: Расчет стоимости пополнения склада до целевого уровня
    @GetMapping("/restock-calculation")
    public ResponseEntity<Map<String, Object>> calculateRestockBudget(
            @RequestParam(defaultValue = "5") int minThreshold,
            @RequestParam(defaultValue = "10") int targetStock) {

        List<Part> allParts = repository.findAll();
        List<Part> partsToRestock = allParts.stream()
                .filter(p -> p.getStock() < minThreshold)
                .collect(Collectors.toList());

        double totalCost = 0.0;
        Map<String, Integer> restockPlan = new HashMap<>();

        for (Part part : partsToRestock) {
            int quantityNeeded = targetStock - part.getStock();
            if (quantityNeeded > 0) {
                totalCost += quantityNeeded * part.getPrice();
                restockPlan.put(part.getName(), quantityNeeded);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalRestockCost", totalCost);
        response.put("itemsToOrderCount", partsToRestock.size());
        response.put("restockPlan", restockPlan);

        return ResponseEntity.ok(response);
    }
}