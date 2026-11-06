import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import AnalysisDashboard from '../components/AnalysisDashboard';
import { getAnalysis } from '../services/analyses';
import { parseApiError } from '../services/errors';
import { formatDateTime } from '../utils/formatDate';

export default function AnalysisResult() {
  const { id } = useParams();
  const [result, setResult] = useState({ forId: null, analysis: null, error: '' });

  useEffect(() => {
    let cancelled = false;

    getAnalysis(id)
      .then((analysis) => {
        if (!cancelled) setResult({ forId: id, analysis, error: '' });
      })
      .catch((err) => {
        if (!cancelled) setResult({ forId: id, analysis: null, error: parseApiError(err).message });
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Still waiting for the analysis that belongs to the current URL.
  if (result.forId !== id) {
    return (
      <section aria-busy="true">
        <p className="sr-only" role="status">Loading analysis...</p>
        <div className="skeleton skeleton-title" />
        <div className="skeleton skeleton-line" />
        <div className="dashboard" aria-hidden="true">
          <div className="dashboard-top">
            <div className="skeleton skeleton-card" />
            <div className="skeleton skeleton-card" />
          </div>
          <div className="dashboard-bottom">
            <div className="skeleton skeleton-card" />
            <div className="skeleton skeleton-card" />
          </div>
        </div>
      </section>
    );
  }

  if (result.error) {
    return (
      <section className="card empty-state">
        <h1>Analysis not available</h1>
        <p className="muted">{result.error}</p>
        <Link to="/analyze" className="btn btn-primary">Analyze a resume</Link>
      </section>
    );
  }

  const analyzedOn = formatDateTime(result.analysis.createdAt);

  return (
    <section>
      <Link to="/history" className="back-link">&larr; Your analyses</Link>
      <header className="result-header">
        <div>
          <h1>Analysis result</h1>
          {analyzedOn && <p className="muted">Analyzed on {analyzedOn}</p>}
        </div>
        <Link to="/analyze" className="btn btn-secondary">Analyze another resume</Link>
      </header>
      <AnalysisDashboard analysis={result.analysis} />
    </section>
  );
}