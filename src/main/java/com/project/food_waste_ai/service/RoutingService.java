package com.project.food_waste_ai.service;

import com.project.food_waste_ai.entity.RouteRecord;
import com.project.food_waste_ai.repository.RouteRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RoutingService {

    @Autowired
    private RouteRecordRepository routeRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── PUBLIC ENTRY POINT ───────────────────────────────────────────────────

    public Map<String, Object> optimizeFleetRoutes(
            List<Map<String, Object>> locations,
            int vehicleCount,
            String strategy,
            String submittedBy) {

        if (locations == null || locations.isEmpty()) {
            return buildErrorResponse("No locations provided");
        }

        int actualVehicles = Math.min(vehicleCount, locations.size());

        Map<String, Object> result;
        try {
            result = nearestNeighbourVRP(locations, actualVehicles, strategy);
        } catch (Exception e) {
            return buildErrorResponse("Optimization failed: " + e.getMessage());
        }

        // Save to database
        saveRecord(locations, actualVehicles, strategy, submittedBy, result);

        return result;
    }

    // ─── SAVE TO DATABASE ─────────────────────────────────────────────────────

    private void saveRecord(List<Map<String, Object>> locations,
                            int vehicleCount,
                            String strategy,
                            String submittedBy,
                            Map<String, Object> result) {
        try {
            RouteRecord record = new RouteRecord();
            record.setSubmittedBy(submittedBy != null ? submittedBy : "anonymous");
            record.setVehicleCount(vehicleCount);
            record.setLocationCount(locations.size());
            record.setStrategyUsed(strategy != null ? strategy : "nearest_neighbour");
            record.setTotalDistanceKm((Double) result.get("total_distance_km"));
            record.setRouteResult(objectMapper.writeValueAsString(result));

            routeRecordRepository.save(record);
        } catch (Exception e) {
            System.err.println("Failed to save route record: " + e.getMessage());
        }
    }

    // ─── NEAREST NEIGHBOUR VRP ────────────────────────────────────────────────

    private Map<String, Object> nearestNeighbourVRP(
            List<Map<String, Object>> locations,
            int vehicleCount,
            String strategy) {

        int n = locations.size();
        double[][] distanceMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distanceMatrix[i][j] = haversineDistance(locations.get(i), locations.get(j));
                }
            }
        }

        List<List<Integer>> vehicleAssignments = new ArrayList<>();
        for (int v = 0; v < vehicleCount; v++) {
            vehicleAssignments.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            vehicleAssignments.get(i % vehicleCount).add(i);
        }

        List<Map<String, Object>> routes = new ArrayList<>();
        double totalDistance = 0;

        for (int v = 0; v < vehicleCount; v++) {
            List<Integer> assigned = vehicleAssignments.get(v);
            if (assigned.isEmpty()) continue;

            List<Integer> optimisedOrder = nearestNeighbour(assigned, distanceMatrix);
            double routeDistance = calculateRouteDistance(optimisedOrder, distanceMatrix);
            totalDistance += routeDistance;

            List<Map<String, Object>> stops = new ArrayList<>();
            for (int idx = 0; idx < optimisedOrder.size(); idx++) {
                int locationIdx = optimisedOrder.get(idx);
                Map<String, Object> stop = new LinkedHashMap<>(locations.get(locationIdx));
                stop.put("stop_number", idx + 1);
                double distFromPrev = idx == 0 ? 0
                        : distanceMatrix[optimisedOrder.get(idx - 1)][locationIdx];
                stop.put("distance_from_prev_km", Math.round(distFromPrev * 100.0) / 100.0);
                stops.add(stop);
            }

            Map<String, Object> route = new LinkedHashMap<>();
            route.put("vehicle_id", "Vehicle-" + (v + 1));
            route.put("total_stops", stops.size());
            route.put("total_distance_km", Math.round(routeDistance * 100.0) / 100.0);
            route.put("stops", stops);
            routes.add(route);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy_used", strategy != null ? strategy : "nearest_neighbour");
        response.put("total_vehicles", vehicleCount);
        response.put("total_locations", n);
        response.put("total_distance_km", Math.round(totalDistance * 100.0) / 100.0);
        response.put("routes", routes);
        return response;
    }

    // ─── NEAREST NEIGHBOUR ALGORITHM ─────────────────────────────────────────

    private List<Integer> nearestNeighbour(List<Integer> indices, double[][] distanceMatrix) {
        Set<Integer> unvisited = new LinkedHashSet<>(indices);
        List<Integer> route = new ArrayList<>();

        int current = indices.get(0);
        route.add(current);
        unvisited.remove(current);

        while (!unvisited.isEmpty()) {
            int nearest = -1;
            double minDist = Double.MAX_VALUE;

            for (int candidate : unvisited) {
                double dist = distanceMatrix[current][candidate];
                if (dist < minDist) {
                    minDist = dist;
                    nearest = candidate;
                }
            }

            route.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }

        return route;
    }

    // ─── HAVERSINE DISTANCE ───────────────────────────────────────────────────

    private double haversineDistance(Map<String, Object> loc1, Map<String, Object> loc2) {
        double lat1 = toDouble(loc1.get("lat"));
        double lng1 = toDouble(loc1.get("lng"));
        double lat2 = toDouble(loc2.get("lat"));
        double lng2 = toDouble(loc2.get("lng"));

        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private double calculateRouteDistance(List<Integer> route, double[][] distanceMatrix) {
        double total = 0;
        for (int i = 1; i < route.size(); i++) {
            total += distanceMatrix[route.get(i - 1)][route.get(i)];
        }
        return total;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0.0;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", message);
        return response;
    }
}