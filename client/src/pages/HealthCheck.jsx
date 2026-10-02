import { useEffect, useState } from 'react';
import { checkHealth } from '../services/api';

export default function HealthCheck() {
  const [status, setStatus] = useState('checking...');

  useEffect(() => {
    checkHealth()
      .then((data) => setStatus(data.status))
      .catch(() => setStatus('unreachable — is the server running on :5000?'));
  }, []);

  return (
    <div style={{ fontFamily: 'sans-serif', padding: '2rem' }}>
      <h1>Resume Analyzer</h1>
      <p>Backend status: <strong>{status}</strong></p>
    </div>
  );
}
