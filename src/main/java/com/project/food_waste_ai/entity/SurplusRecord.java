package com.project.food_waste_ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "surplus_records")
public class SurplusRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who submitted this record (will be linked to a user once auth is added)
    private String submittedBy;

    // The raw historical data that was sent in
    @Column(columnDefinition = "TEXT")
    private String historicalSurplus; // stored as comma-separated string e.g. "45.2,50.1,38.7"

    // How many days were predicted
    private int daysToPredict;

    // Which model was used (Holt-Winters or Moving Average)
    private String modelUsed;

    // The predicted values returned
    @Column(columnDefinition = "TEXT")
    private String forecastResult; // stored as JSON string

    // When this record was created
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
