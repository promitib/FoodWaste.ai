package com.project.food_waste_ai.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RouteOptimizationRequest {
    private List<Map<String, Object>> locations;
    private int vehicleCount;
    private String optimizationStrategy;
}