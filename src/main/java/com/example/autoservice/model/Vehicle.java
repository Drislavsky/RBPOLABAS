package com.example.autoservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "vin", unique = true, nullable = false, length = 17)
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Customer owner;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "license_plate", unique = true, length = 15)
    private String licensePlate;

    public Vehicle() {
    }

    public Vehicle(String brand, String model, Integer manufactureYear, String vin, Customer owner) {
        this.brand = brand;
        this.model = model;
        this.manufactureYear = manufactureYear;
        this.vin = vin;
        this.owner = owner;
    }

    public Vehicle(String brand, String model, Integer manufactureYear, String vin, Customer owner, String description, String licensePlate) {
        this.brand = brand;
        this.model = model;
        this.manufactureYear = manufactureYear;
        this.vin = vin;
        this.owner = owner;
        this.description = description;
        this.licensePlate = licensePlate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    // Convenience method for getting year (alias for manufactureYear)
    public Integer getYear() {
        return manufactureYear;
    }

    public void setYear(Integer year) {
        this.manufactureYear = year;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", manufactureYear=" + manufactureYear +
                ", vin='" + vin + '\'' +
                ", owner=" + (owner != null ? owner.getName() : "null") +
                ", description='" + description + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vehicle vehicle = (Vehicle) o;

        if (!id.equals(vehicle.id)) return false;
        if (!brand.equals(vehicle.brand)) return false;
        if (!model.equals(vehicle.model)) return false;
        if (!manufactureYear.equals(vehicle.manufactureYear)) return false;
        return vin.equals(vehicle.vin);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + brand.hashCode();
        result = 31 * result + model.hashCode();
        result = 31 * result + manufactureYear.hashCode();
        result = 31 * result + vin.hashCode();
        return result;
    }
}