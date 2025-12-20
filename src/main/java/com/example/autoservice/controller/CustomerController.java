package com.example.autoservice.controller;

import com.example.autoservice.model.Customer;
import com.example.autoservice.model.ServiceOrder;
import com.example.autoservice.repository.CustomerRepository;
import com.example.autoservice.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository repo;
    private final ServiceOrderRepository serviceOrderRepository;

    public CustomerController(CustomerRepository repo, ServiceOrderRepository serviceOrderRepository) {
        this.repo = repo;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @GetMapping
    public List<Customer> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        Customer saved = repo.save(customer);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setPhone(updated.getPhone());
            existing.setEmail(updated.getEmail());
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

    // Бизнес-операция: Получение общей суммы трат клиента (LTV)
    @GetMapping("/{id}/total-spent")
    public ResponseEntity<Double> getCustomerTotalSpent(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByCustomerId(id);

        double totalSpent = orders.stream()
                .filter(ServiceOrder::isCompleted) // Учитываем только завершенные заказы
                .mapToDouble(ServiceOrder::getTotalCost)
                .sum();

        return ResponseEntity.ok(totalSpent);
    }
}