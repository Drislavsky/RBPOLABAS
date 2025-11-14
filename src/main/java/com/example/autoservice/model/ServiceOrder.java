package com.example.autoservice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders")
public class ServiceOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    @ManyToMany
    @JoinTable(
            name = "order_parts",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "part_id")
    )
    private List<Part> parts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "order_required_tasks", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "task")
    private List<String> requiredTasks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "order_completed_tasks", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "task")
    private List<String> completedTasks = new ArrayList<>();

    private double laborCost;
    private boolean completed;
    private String description;

    public ServiceOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Mechanic getMechanic() { return mechanic; }
    public void setMechanic(Mechanic mechanic) { this.mechanic = mechanic; }

    public List<Part> getParts() { return parts; }
    public void setParts(List<Part> parts) { this.parts = parts; }

    public List<String> getRequiredTasks() { return requiredTasks; }
    public void setRequiredTasks(List<String> requiredTasks) { this.requiredTasks = requiredTasks; }

    public List<String> getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(List<String> completedTasks) { this.completedTasks = completedTasks; }

    public double getLaborCost() { return laborCost; }
    public void setLaborCost(double laborCost) { this.laborCost = laborCost; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTotalCost() {
        double partsSum = 0.0;
        if (parts != null) {
            for (Part p : parts) {
                partsSum += p != null ? p.getPrice() : 0.0;
            }
        }
        return laborCost + partsSum;
    }

    public boolean canBeClosed() {
        return requiredTasks == null || requiredTasks.isEmpty() || (completedTasks != null && completedTasks.containsAll(requiredTasks));
    }
}
