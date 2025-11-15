package com.example.autoservice.controller;

import com.example.autoservice.model.Part;
import com.example.autoservice.repository.PartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        // Set availability based on stock
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
            // Update availability based on stock
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

    // Business Operation 1: Update part stock quantity
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Part> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Optional<Part> partOpt = repository.findById(id);
        if (partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Part part = partOpt.get();

        if (quantity < 0) {
            return ResponseEntity.badRequest().build();
        }

        part.setStock(quantity);
        part.setIsAvailable(quantity > 0);

        Part saved = repository.save(part);
        return ResponseEntity.ok(saved);
    }

    // Business Operation 2: Increase part stock
    @PostMapping("/{id}/stock/increase")
    public ResponseEntity<Part> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Optional<Part> partOpt = repository.findById(id);
        if (partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Part part = partOpt.get();
        part.increaseStock(quantity);

        Part saved = repository.save(part);
        return ResponseEntity.ok(saved);
    }

    // Business Operation 3: Decrease part stock
    @PostMapping("/{id}/stock/decrease")
    public ResponseEntity<Part> decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Optional<Part> partOpt = repository.findById(id);
        if (partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Part part = partOpt.get();

        try {
            part.decreaseStock(quantity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }

        Part saved = repository.save(part);
        return ResponseEntity.ok(saved);
    }

    // Business Operation 4: Get parts by category
    @GetMapping("/category/{category}")
    public List<Part> getPartsByCategory(@PathVariable String category) {
        return repository.findAll().stream()
                .filter(part -> part.getCategory() != null && part.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    // Business Operation 5: Get available parts (in stock)
    @GetMapping("/available")
    public List<Part> getAvailableParts() {
        return repository.findAll().stream()
                .filter(Part::isInStock)
                .toList();
    }

    // Business Operation 6: Get low stock parts (stock less than threshold)
    @GetMapping("/low-stock")
    public List<Part> getLowStockParts(@RequestParam(defaultValue = "5") Integer threshold) {
        return repository.findAll().stream()
                .filter(part -> part.getStock() <= threshold)
                .toList();
    }

    // Business Operation 7: Search parts by name
    @GetMapping("/search")
    public List<Part> searchPartsByName(@RequestParam String name) {
        return repository.findAll().stream()
                .filter(part -> part.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    // Business Operation 8: Get parts by manufacturer
    @GetMapping("/manufacturer/{manufacturer}")
    public List<Part> getPartsByManufacturer(@PathVariable String manufacturer) {
        return repository.findAll().stream()
                .filter(part -> part.getManufacturer() != null && part.getManufacturer().equalsIgnoreCase(manufacturer))
                .toList();
    }

    // Business Operation 9: Update part price
    @PatchMapping("/{id}/price")
    public ResponseEntity<Part> updatePrice(@PathVariable Long id, @RequestParam Double price) {
        Optional<Part> partOpt = repository.findById(id);
        if (partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (price <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Part part = partOpt.get();
        part.setPrice(price);

        Part saved = repository.save(part);
        return ResponseEntity.ok(saved);
    }

    // Business Operation 10: Get total inventory value
    @GetMapping("/inventory/value")
    public ResponseEntity<Double> getTotalInventoryValue() {
        double totalValue = repository.findAll().stream()
                .mapToDouble(Part::getTotalValue)
                .sum();
        return ResponseEntity.ok(totalValue);
    }

    // Business Operation 11: Bulk update part prices by category
    @PutMapping("/category/{category}/price")
    @Transactional
    public ResponseEntity<List<Part>> updatePricesByCategory(
            @PathVariable String category,
            @RequestParam Double percentageChange) {

        List<Part> parts = repository.findAll().stream()
                .filter(part -> part.getCategory() != null && part.getCategory().equalsIgnoreCase(category))
                .toList();

        if (parts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        for (Part part : parts) {
            double newPrice = part.getPrice() * (1 + percentageChange / 100);
            part.setPrice(newPrice);
            repository.save(part);
        }

        return ResponseEntity.ok(parts);
    }

    // Business Operation 12: Get part categories
    @GetMapping("/categories")
    public List<String> getPartCategories() {
        return repository.findAll().stream()
                .map(Part::getCategory)
                .distinct()
                .toList();
    }

    // Business Operation 13: Check part availability
    @GetMapping("/{id}/availability")
    public ResponseEntity<String> checkAvailability(@PathVariable Long id) {
        Optional<Part> partOpt = repository.findById(id);
        if (partOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Part part = partOpt.get();
        String status = part.isInStock() ?
                "IN_STOCK - " + part.getStock() + " units available" :
                "OUT_OF_STOCK";

        return ResponseEntity.ok(status);
    }

    // Business Operation 14: Restock multiple parts
    @PostMapping("/bulk/restock")
    @Transactional
    public ResponseEntity<List<Part>> bulkRestock(@RequestBody List<PartRestockRequest> restockRequests) {
        List<Part> updatedParts = restockRequests.stream()
                .map(request -> {
                    Optional<Part> partOpt = repository.findById(request.getPartId());
                    if (partOpt.isPresent()) {
                        Part part = partOpt.get();
                        part.increaseStock(request.getQuantity());
                        return repository.save(part);
                    }
                    return null;
                })
                .filter(part -> part != null)
                .toList();

        return ResponseEntity.ok(updatedParts);
    }

    // Business Operation 15: Get parts with price range
    @GetMapping("/price-range")
    public List<Part> getPartsByPriceRange(@RequestParam Double minPrice, @RequestParam Double maxPrice) {
        return repository.findAll().stream()
                .filter(part -> part.getPrice() >= minPrice && part.getPrice() <= maxPrice)
                .toList();
    }

    // Request DTO for bulk restock operation
    public static class PartRestockRequest {
        private Long partId;
        private Integer quantity;

        public Long getPartId() {
            return partId;
        }

        public void setPartId(Long partId) {
            this.partId = partId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}