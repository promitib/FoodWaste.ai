package com.project.food_waste_ai.service;

import com.project.food_waste_ai.entity.SurplusRecord;
import com.project.food_waste_ai.repository.SurplusRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class ForecastingService {

    @Autowired
    private SurplusRecordRepository surplusRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── PUBLIC ENTRY POINT ───────────────────────────────────────────────────

    public Map<String, Object> predictSurplus(List<Double> historicalSurplus,
                                              int daysToPredict,
                                              String submittedBy) {
        List<Double> data = historicalSurplus.size() < 14
                ? generateSyntheticData()
                : historicalSurplus;

        Map<String, Object> result;
        try {
            result = holtWinters(data, daysToPredict);
        } catch (Exception e) {
            result = movingAverageFallback(data, daysToPredict);
        }

        // Save to database
        saveRecord(historicalSurplus, daysToPredict, submittedBy, result);

        return result;
    }

    // ─── SAVE TO DATABASE ─────────────────────────────────────────────────────

    private void saveRecord(List<Double> historicalSurplus,
                            int daysToPredict,
                            String submittedBy,
                            Map<String, Object> result) {
        try {
            SurplusRecord record = new SurplusRecord();
            record.setSubmittedBy(submittedBy != null ? submittedBy : "anonymous");
            record.setDaysToPredict(daysToPredict);
            record.setModelUsed((String) result.get("model_used"));

            // Convert list to comma-separated string for storage
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < historicalSurplus.size(); i++) {
                sb.append(historicalSurplus.get(i));
                if (i < historicalSurplus.size() - 1) sb.append(",");
            }
            record.setHistoricalSurplus(sb.toString());

            // Convert forecast result map to JSON string
            record.setForecastResult(objectMapper.writeValueAsString(result));

            surplusRecordRepository.save(record);
        } catch (Exception e) {
            // Don't let a save failure break the API response
            System.err.println("Failed to save surplus record: " + e.getMessage());
        }
    }

    // ─── HOLT-WINTERS EXPONENTIAL SMOOTHING ───────────────────────────────────

    private Map<String, Object> holtWinters(List<Double> data, int daysToPredict) {
        int seasonLength = 7;
        int n = data.size();

        double alpha = 0.3;
        double beta  = 0.1;
        double gamma = 0.2;

        double level = data.subList(0, seasonLength).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

        double trend = 0;
        for (int i = 0; i < seasonLength; i++) {
            trend += (data.get(i + seasonLength) - data.get(i)) / seasonLength;
        }
        trend /= seasonLength;

        double[] seasonal = new double[seasonLength];
        for (int i = 0; i < seasonLength; i++) {
            seasonal[i] = data.get(i) / level;
        }

        for (int i = seasonLength; i < n; i++) {
            double value = data.get(i);
            double prevLevel = level;

            level = alpha * (value / seasonal[i % seasonLength])
                  + (1 - alpha) * (level + trend);
            trend = beta * (level - prevLevel) + (1 - beta) * trend;
            seasonal[i % seasonLength] = gamma * (value / level)
                  + (1 - gamma) * seasonal[i % seasonLength];
        }

        List<Map<String, Object>> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= daysToPredict; i++) {
            double predicted = (level + i * trend) * seasonal[i % seasonLength];
            predicted = Math.max(0, predicted);

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", today.plusDays(i).toString());
            point.put("predicted_kg", Math.round(predicted * 100.0) / 100.0);
            forecast.add(point);
        }

        List<Map<String, Object>> historical = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", today.minusDays(n - i).toString());
            point.put("surplus_kg", data.get(i));
            historical.add(point);
        }

        return buildResponse(historical, forecast, "Holt-Winters Exponential Smoothing");
    }

    // ─── SIMPLE MOVING AVERAGE FALLBACK ──────────────────────────────────────

    private Map<String, Object> movingAverageFallback(List<Double> data, int daysToPredict) {
        int window = Math.min(7, data.size());
        double avg = data.subList(data.size() - window, data.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

        List<Map<String, Object>> historical = new ArrayList<>();
        List<Map<String, Object>> forecast   = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", today.minusDays(data.size() - i).toString());
            point.put("surplus_kg", data.get(i));
            historical.add(point);
        }

        for (int i = 1; i <= daysToPredict; i++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", today.plusDays(i).toString());
            point.put("predicted_kg", Math.round(avg * 100.0) / 100.0);
            forecast.add(point);
        }

        return buildResponse(historical, forecast, "Simple Moving Average (Fallback)");
    }

    // ─── SYNTHETIC DATA GENERATOR ─────────────────────────────────────────────

    private List<Double> generateSyntheticData() {
        List<Double> data = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < 30; i++) {
            double value = 50
                    + 20 * Math.sin(2 * Math.PI * i / 7.0)
                    + random.nextGaussian() * 5;
            data.add(Math.max(0, Math.round(value * 100.0) / 100.0));
        }
        return data;
    }

    // ─── RESPONSE BUILDER ─────────────────────────────────────────────────────

    private Map<String, Object> buildResponse(List<Map<String, Object>> historical,
                                              List<Map<String, Object>> forecast,
                                              String modelUsed) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("historical", historical);
        response.put("forecast", forecast);
        response.put("model_used", modelUsed);
        return response;
    }
}