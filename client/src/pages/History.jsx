import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FileIcon } from '../components/icons';
import ScoreRing from '../components/ScoreRing';
import { listAnalyses } from '../services/analyses';
import { parseApiError } from '../services/errors';
import { formatDateTime } from '../utils/formatDate';

export default function History() {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState({ forAttempt: null, analyses: [], error: '' });

  useEffect(() => {
    let cancelled = false;

    listAnalyses()
      .then((analyses) => {
        if (!cancelled) setResult({ forAttempt: attempt, analyses, error: '' });
      })
      .catch((err) => {
        if (!cancelled) setResult({ forAttempt: attempt, analyses: [], error: parseApiError(err).message });
      });

    return () => {
      cancelled = true;
    };
  }, [attempt]);

  const loading = result.forAttempt !== attempt;
  const { analyses, error } = result;

  return (
    <section>
      <header className="result-header">
        <div>
          <h1>Your analyses</h1>
          {!loading && !error && analyses.length > 0 && (
            <p className="muted">
              {analyses.length} {analyses.length === 1 ? 'analysis' : 'analyses'}, newest first
            </p>
          )}
        </div>
        <Link to="/analyze" className="btn btn-primary">New analysis</Link>
      </header>

      {loading && (
        <div className="history-list" aria-busy="true">
          <p className="sr-only" role="status">Loading your analyses...</p>
          {[0, 1, 2, 3].map((n) => (
            <div className="skeleton skeleton-row" key={n} aria-hidden="true" />
          ))}
        </div>
      )}

      {!loading && error && (
        <div className="card empty-state">
          <p className="form-banner error" role="alert">{error}</p>
          <button type="button" className="btn btn-secondary" onClick={() => setAttempt((n) => n + 1)}>
            Try again
          </button>
        </div>
      )}

      {!loading && !error && analyses.length === 0 && (
        <div className="card empty-state">
          <span className="empty-icon">
            <FileIcon size={28} />
          </span>
          <h2>No analyses yet</h2>
          <p className="muted">Upload a resume and a job description to see your first result here.</p>
          <Link to="/analyze" className="btn btn-primary">Analyze a resume</Link>
        </div>
      )}

      {!loading && !error && analyses.length > 0 && (
        <ul className="history-list">
          {analyses.map((analysis) => {
            const analyzedOn = formatDateTime(analysis.createdAt);
            return (
              <li key={analysis.id}>
                <Link to={`/analyses/${analysis.id}`} className="history-item">
                  <span className="history-icon">
                    <FileIcon size={22} />
                  </span>
                  <span className="history-main">
                    <span className="history-name">{analysis.resumeFilename || 'Untitled resume'}</span>
                    {analyzedOn && <span className="muted small">{analyzedOn}</span>}
                  </span>
                  {Number.isFinite(analysis.matchScore) ? (
                    <ScoreRing score={analysis.matchScore} />
                  ) : (
                    <span className="history-score">{'\u2013'}</span>
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}