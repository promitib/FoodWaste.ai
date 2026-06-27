package com.project.food_waste_ai.model;

import lombok.Data;
import java.util.List;

@Data
public class ForecastRequest {
    private List<Double> historicalSurplus;
    private int daysToPredict;
}
