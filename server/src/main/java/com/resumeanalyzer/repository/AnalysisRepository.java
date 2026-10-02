package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    // "Resume_User_Id" walks Analysis -> resume -> user -> id, i.e. a history query
    // for one logged-in user, without writing any SQL by hand.
    List<Analysis> findByResume_User_IdOrderByCreatedAtDesc(Long userId);
}
