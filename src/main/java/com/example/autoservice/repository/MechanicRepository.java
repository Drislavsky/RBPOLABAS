package com.example.autoservice.repository;

import com.example.autoservice.model.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
}