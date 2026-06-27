package com.project.food_waste_ai.repository;

import com.project.food_waste_ai.entity.SurplusRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SurplusRecordRepository extends JpaRepository<SurplusRecord, Long> {

    // Spring automatically writes this query for you:
    // SELECT * FROM surplus_records WHERE submitted_by = ? ORDER BY created_at DESC
    List<SurplusRecord> findBySubmittedByOrderByCreatedAtDesc(String submittedBy);

    // SELECT * FROM surplus_records ORDER BY created_at DESC LIMIT 10
    List<SurplusRecord> findTop10ByOrderByCreatedAtDesc();
}