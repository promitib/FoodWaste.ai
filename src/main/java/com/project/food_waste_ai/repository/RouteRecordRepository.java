package com.project.food_waste_ai.repository;

import com.project.food_waste_ai.entity.RouteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRecordRepository extends JpaRepository<RouteRecord, Long> {

    // SELECT * FROM route_records WHERE submitted_by = ? ORDER BY created_at DESC
    List<RouteRecord> findBySubmittedByOrderByCreatedAtDesc(String submittedBy);

    // SELECT * FROM route_records ORDER BY created_at DESC LIMIT 10
    List<RouteRecord> findTop10ByOrderByCreatedAtDesc();
}
