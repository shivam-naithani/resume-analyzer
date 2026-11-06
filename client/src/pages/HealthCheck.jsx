import { useEffect, useState } from 'react';
import { checkHealth } from '../services/api';

const LABELS = {
  checking: 'Checking...',
  up: 'Backend is running',
  issue: 'Unexpected response',
  down: 'Backend is unreachable',
};

export default function HealthCheck() {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState({ forAttempt: null, state: 'checking', status: '' });

  useEffect(() => {
    let cancelled = false;

    checkHealth()
      .then((data) => {
        if (cancelled) return;
        const status = String(data?.status ?? '');
        setResult({ forAttempt: attempt, state: status === 'ok' ? 'up' : 'issue', status });
      })
      .catch(() => {
        if (!cancelled) setResult({ forAttempt: attempt, state: 'down', status: '' });
      });

    return () => {
      cancelled = true;
    };
  }, [attempt]);

  const checking = result.forAttempt !== attempt;
  const state = checking ? 'checking' : result.state;

  return (
    <section className="card empty-state card-narrow">
      <h1>Backend status</h1>
      <p className={`status-pill status-${state}`} role="status">
        <span className="status-dot" aria-hidden="true" />
        {LABELS[state]}
      </p>
      {state === 'down' && (
        <p className="muted">
          The API did not respond. Make sure the server is running and the API URL is correct.
        </p>
      )}
      {state === 'issue' && (
        <p className="muted">
          The server answered with status &quot;{result.status || 'none'}&quot; instead of &quot;ok&quot;.
        </p>
      )}
      <button
        type="button"
        className="btn btn-secondary"
        disabled={checking}
        onClick={() => setAttempt((n) => n + 1)}
      >
        Check again
      </button>
    </section>
  );
}