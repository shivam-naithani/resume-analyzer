package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserId(Long userId);
    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    // Resumes uploaded before the cutoff that never got an analysis.
    @Query("select r.id from Resume r where r.uploadedAt < :cutoff "
            + "and not exists (select 1 from Analysis a where a.resume = r)")
    List<Long> findUnusedIdsUploadedBefore(@Param("cutoff") LocalDateTime cutoff);
}