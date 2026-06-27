package com.project.food_waste_ai.controller;

import com.project.food_waste_ai.model.ForecastRequest;
import com.project.food_waste_ai.model.RouteOptimizationRequest;
import com.project.food_waste_ai.service.ForecastingService;
import com.project.food_waste_ai.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // allows your React/Vite frontend to talk to this API
public class FoodWasteController {

    // ─── DEPENDENCY INJECTION ─────────────────────────────────────────────────
    // Spring automatically hands us the services we need.
    // We never write "new ForecastingService()" — Spring manages that for us.

    @Autowired
    private ForecastingService forecastingService;

    @Autowired
    private RoutingService routingService;

    // ─── HEALTH CHECK ─────────────────────────────────────────────────────────
    // GET /api
    // Same as the Python root "/" endpoint.
    // Useful for checking the server is alive.

    @GetMapping
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "FoodWaste.AI Backend API");
        response.put("status", "running");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    // ─── FORECAST ENDPOINT ────────────────────────────────────────────────────
    // POST /api/forecast
    // Accepts a JSON body matching ForecastRequest, returns surplus predictions.
    //
    // Example request body:
    // {
    //   "historicalSurplus": [45.2, 50.1, 38.7, 60.0, 55.3, 42.1, 48.9],
    //   "daysToPredict": 7
    // }

    @PostMapping("/forecast")
        public ResponseEntity<Map<String, Object>> forecast(@RequestBody ForecastRequest request) {
            try {
                Map<String, Object> result = forecastingService.predictSurplus(
                    request.getHistoricalSurplus(),
                    request.getDaysToPredict(),
                    "anonymous" // will be replaced with real user after auth
                );
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Forecast failed: " + e.getMessage()));
            }
        }

    // ─── ROUTE OPTIMISATION ENDPOINT ──────────────────────────────────────────
    // POST /api/optimize-route
    // Accepts a JSON body matching RouteOptimizationRequest, returns optimised routes.
    //
    // Example request body:
    // {
    //   "locations": [
    //     {"name": "Food Bank A", "lat": 19.076, "lng": 72.877},
    //     {"name": "Restaurant B", "lat": 19.082, "lng": 72.881},
    //     {"name": "Shelter C",    "lat": 19.071, "lng": 72.869}
    //   ],
    //   "vehicleCount": 2,
    //   "optimizationStrategy": "nearest_neighbour"
    // }

    @PostMapping("/optimize-route")
        public ResponseEntity<Map<String, Object>> optimizeRoute(@RequestBody RouteOptimizationRequest request) {
            try {
                Map<String, Object> result = routingService.optimizeFleetRoutes(
                    request.getLocations(),
                    request.getVehicleCount(),
                    request.getOptimizationStrategy(),
                    "anonymous" // will be replaced with real user after auth
                );
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Route optimization failed: " + e.getMessage()));
            }
        }
}
