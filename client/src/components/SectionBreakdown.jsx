// Fixed order, because the API's JSON key order isn't reliable.
const SECTIONS = [
  { key: 'education', label: 'Education', scored: false },
  { key: 'skills', label: 'Skills', scored: true },
  { key: 'experience', label: 'Experience', scored: true },
  { key: 'projects', label: 'Projects', scored: true },
];

export default function SectionBreakdown({ sectionScores }) {
  const available = SECTIONS.filter(({ key }) => sectionScores?.[key]);

  if (available.length === 0) {
    return <p className="muted">Section details aren&apos;t available for this analysis.</p>;
  }

  return (
    <ul className="section-list">
      {available.map(({ key, label, scored }) => {
        const details = sectionScores[key];
        const coverage = typeof details.keywordCoverage === 'number' ? details.keywordCoverage : 0;

        return (
          <li className="section-row" key={key}>
            <div className="section-row-head">
              <span className="section-name">{label}</span>
              <span className={details.present ? 'badge badge-ok' : 'badge badge-muted'}>
                {details.present ? 'Detected' : 'Not detected'}
              </span>
            </div>

            {!details.present && <p className="section-detail">We couldn&apos;t detect this section.</p>}

            {details.present && scored && (
              <>
                <div
                  className="bar"
                  role="progressbar"
                  aria-label={`${label} keyword coverage`}
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-valuenow={coverage}
                >
                  <div className="bar-fill" style={{ width: `${Math.min(100, coverage)}%` }} />
                </div>
                <p className="section-detail">
                  {coverage}% of the job&apos;s keywords &middot; {details.wordCount} words
                </p>
              </>
            )}

            {details.present && !scored && <p className="section-detail">{details.wordCount} words</p>}
          </li>
        );
      })}
    </ul>
  );
}