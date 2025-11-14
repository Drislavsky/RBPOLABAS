package com.example.autoservice.controller;

import com.example.autoservice.model.Mechanic;
import com.example.autoservice.repository.MechanicRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    private final MechanicRepository repo;

    public MechanicController(MechanicRepository repo) { this.repo = repo; }

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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
