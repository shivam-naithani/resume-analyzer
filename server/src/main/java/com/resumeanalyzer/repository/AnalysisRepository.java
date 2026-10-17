package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Analysis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    // "Resume_User_Id" walks Analysis -> resume -> user -> id. The entity graph
    // loads the resume in the same query, avoiding one extra query per row.
    @EntityGraph(attributePaths = "resume")
    List<Analysis> findByResume_User_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "resume")
    Optional<Analysis> findByIdAndResume_User_Id(Long id, Long userId);
}