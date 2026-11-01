import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listAnalyses } from '../services/analyses';
import { parseApiError } from '../services/errors';
import { formatDateTime } from '../utils/formatDate';

function formatScore(score) {
  return Number.isFinite(score) ? `${score.toFixed(1)}%` : '\u2013';
}

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

      {loading && <p className="status-text">Loading your analyses...</p>}

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
                  <span className="history-main">
                    <span className="history-name">{analysis.resumeFilename || 'Untitled resume'}</span>
                    {analyzedOn && <span className="muted small">{analyzedOn}</span>}
                  </span>
                  <span className="history-score">{formatScore(analysis.matchScore)}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}