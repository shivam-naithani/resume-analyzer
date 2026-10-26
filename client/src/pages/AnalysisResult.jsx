import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import AnalysisDashboard from '../components/AnalysisDashboard';
import { getAnalysis } from '../services/analyses';
import { parseApiError } from '../services/errors';

function formatDate(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? null
    : date.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

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
    return <p className="status-text">Loading analysis...</p>;
  }

  if (result.error) {
    return (
      <section className="page">
        <h1>Analysis not available</h1>
        <p>{result.error}</p>
        <Link to="/analyze" className="btn btn-primary">Analyze a resume</Link>
      </section>
    );
  }

  const analyzedOn = formatDate(result.analysis.createdAt);

  return (
    <section>
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