import ScoreRing from './ScoreRing';
import SectionBreakdown from './SectionBreakdown';

export default function AnalysisDashboard({ analysis }) {
  const { matchScore, sectionScores, jobDescriptionText } = analysis;
  const keywords = analysis.missingKeywords ?? [];
  const suggestions = analysis.suggestions ?? [];

  return (
    <div className="dashboard">
      <div className="dashboard-top">
        <section className="card score-card">
          <h2>Match score</h2>
          <ScoreRing score={matchScore} />
          <p className="muted small">
            Scores are relative. Compare the same resume across different job descriptions rather
            than reading one number as a grade.
          </p>
        </section>

        <section className="card">
          <h2>Section breakdown</h2>
          <p className="muted small">
            Keyword coverage is the share of the job description&apos;s keywords that appear in each
            section.
          </p>
          <SectionBreakdown sectionScores={sectionScores} />
        </section>
      </div>

      <div className="dashboard-bottom">
        <section className="card">
          <h2>Missing keywords</h2>
          {keywords.length > 0 ? (
            <>
              <p className="muted small">
                Words from the job description that don&apos;t appear in your resume. Add them where
                they genuinely apply.
              </p>
              <ul className="chips">
                {keywords.map((keyword) => (
                  <li className="chip" key={keyword}>{keyword}</li>
                ))}
              </ul>
            </>
          ) : (
            <p className="muted">No keywords from the job description are missing.</p>
          )}
        </section>

        <section className="card">
          <h2>Suggestions</h2>
          {suggestions.length > 0 ? (
            <ul className="suggestion-list">
              {suggestions.map((tip) => (
                <li key={tip}>{tip}</li>
              ))}
            </ul>
          ) : (
            <p className="muted">No suggestions available for this analysis.</p>
          )}
        </section>
      </div>

      <details className="card job-description">
        <summary>Job description used</summary>
        <p>{jobDescriptionText}</p>
      </details>
    </div>
  );
}