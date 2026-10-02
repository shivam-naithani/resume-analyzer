package com.resumeanalyzer.entity;

import com.resumeanalyzer.converter.StringListConverter;
import com.resumeanalyzer.converter.StringObjectMapConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many analyses can point to one resume, so a resume can be scored
    // against multiple job descriptions without any schema change.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Lob
    @Column(name = "job_description_text", nullable = false, columnDefinition = "TEXT")
    private String jobDescriptionText;

    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    @Convert(converter = StringListConverter.class)
    @Column(name = "missing_keywords", columnDefinition = "json")
    private List<String> missingKeywords;

    @Convert(converter = StringObjectMapConverter.class)
    @Column(name = "section_scores", columnDefinition = "json")
    private Map<String, Object> sectionScores;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- getters and setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getJobDescriptionText() { return jobDescriptionText; }
    public void setJobDescriptionText(String jobDescriptionText) { this.jobDescriptionText = jobDescriptionText; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }

    public Map<String, Object> getSectionScores() { return sectionScores; }
    public void setSectionScores(Map<String, Object> sectionScores) { this.sectionScores = sectionScores; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
