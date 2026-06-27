package com.project.food_waste_ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "route_records")
public class RouteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who submitted this request
    private String submittedBy;

    // Number of vehicles used
    private int vehicleCount;

    // Number of locations optimised
    private int locationCount;

    // Total distance across all routes
    private double totalDistanceKm;

    // Strategy used
    private String strategyUsed;

    // Full route result stored as JSON
    @Column(columnDefinition = "TEXT")
    private String routeResult;

    // When this was created
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
