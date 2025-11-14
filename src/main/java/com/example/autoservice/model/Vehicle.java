package com.example.autoservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private Integer manufactureYear;
    private String vin;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Customer owner;

    public Vehicle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return manufactureYear; }
    public void setYear(Integer year) { this.manufactureYear = year; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }
}
