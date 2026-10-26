const RADIUS = 52;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

export default function ScoreRing({ score }) {
  const value = Math.min(100, Math.max(0, Number.isFinite(score) ? score : 0));
  const filled = (value / 100) * CIRCUMFERENCE;

  return (
    <div className="score-ring" role="img" aria-label={`Match score: ${value.toFixed(1)} percent`}>
      <svg viewBox="0 0 120 120" aria-hidden="true">
        <circle className="score-ring-track" cx="60" cy="60" r={RADIUS} />
        <circle
          className="score-ring-fill"
          cx="60"
          cy="60"
          r={RADIUS}
          strokeDasharray={`${filled} ${CIRCUMFERENCE}`}
          transform="rotate(-90 60 60)"
        />
      </svg>
      <span className="score-ring-value">{value.toFixed(1)}%</span>
    </div>
  );
}