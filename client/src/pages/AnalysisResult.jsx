import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import AnalysisDashboard from '../components/AnalysisDashboard';
import { deleteAnalysis, getAnalysis } from '../services/analyses';
import { parseApiError } from '../services/errors';
import { formatDateTime } from '../utils/formatDate';

export default function AnalysisResult() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [result, setResult] = useState({ forId: null, analysis: null, error: '' });
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

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

  const handleDelete = async () => {
    setDeleting(true);
    setDeleteError('');
    try {
      await deleteAnalysis(id);
    } catch (err) {
      // 404 means it is already gone (for example, deleted in another tab), which is the goal.
      if (err?.response?.status !== 404) {
        setDeleteError(parseApiError(err).message);
        setDeleting(false);
        return;
      }
    }
    // Replace the entry so the Back button doesn't return to a deleted result.
    navigate('/history', { replace: true });
  };

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
        <div className="result-actions">
          <Link to="/analyze" className="btn btn-secondary">Analyze another resume</Link>
          <button
            type="button"
            className="btn btn-danger"
            aria-expanded={confirming}
            aria-controls="delete-confirm"
            disabled={deleting}
            onClick={() => setConfirming(true)}
          >
            Delete analysis
          </button>
        </div>
      </header>

      {confirming && (
        <div id="delete-confirm" className="confirm-bar" role="alert">
          <p>
            Delete this analysis? This can&apos;t be undone. If no other analysis uses the uploaded
            resume, its extracted text is deleted too.
          </p>
          <div className="confirm-actions">
            <button type="button" className="btn btn-danger-solid" disabled={deleting} onClick={handleDelete}>
              {deleting ? 'Deleting...' : 'Yes, delete'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              disabled={deleting}
              onClick={() => {
                setConfirming(false);
                setDeleteError('');
              }}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
      {deleteError && <p className="form-banner error" role="alert">{deleteError}</p>}

      <AnalysisDashboard analysis={result.analysis} />
    </section>
  );
}