import { Link } from 'react-router-dom';
import {
  BoltIcon,
  ChartIcon,
  LayersIcon,
  ListIcon,
  ShieldIcon,
  UploadIcon,
} from '../components/icons';
import ScoreRing from '../components/ScoreRing';

// Illustrative values for the hero preview, not a real analysis.
const SAMPLE = {
  score: 72,
  role: 'Backend Engineer',
  keywords: ['docker', 'kubernetes', 'microservices', 'redis'],
  skillsCoverage: 74,
};

const trust = [
  { icon: ShieldIcon, label: 'Private and secure' },
  { icon: BoltIcon, label: 'Fast analysis' },
  { icon: LayersIcon, label: 'Detailed feedback' },
];

const steps = [
  { icon: UploadIcon, title: 'Upload your resume', text: 'Add a PDF. Text is extracted for analysis.' },
  { icon: ListIcon, title: 'Paste a job description', text: 'Choose the role you are applying for.' },
  { icon: ChartIcon, title: 'Review your results', text: 'Score, keywords, sections and suggestions.' },
];

export default function Home() {
  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="hero-badge">Free resume and job match check</span>
          <h1>
            Know how your resume matches the job <span>before you apply</span>
          </h1>
          <p className="hero-lead">
            Get a match score, the keywords you are missing, a section-by-section breakdown and
            concrete suggestions. No external AI service, your resume stays on our server.
          </p>
          <div className="hero-actions">
            <Link to="/analyze" className="btn btn-primary btn-lg">Analyze my resume</Link>
            <Link to="/signup" className="btn btn-secondary btn-lg">Create an account</Link>
          </div>
          <ul className="hero-trust">
            {trust.map(({ icon: Icon, label }) => (
              <li key={label}>
                <Icon size={20} />
                {label}
              </li>
            ))}
          </ul>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="hero-visual-shape" />
          <div className="hero-preview">
            <p className="hero-preview-label">Sample result</p>
            <div className="hero-preview-top">
              <ScoreRing score={SAMPLE.score} />
              <div>
                <div className="hero-preview-title">Match score</div>
                <div className="hero-preview-role">{SAMPLE.role}</div>
              </div>
            </div>

            <div className="hero-preview-section">
              <div className="hero-preview-heading">Missing keywords</div>
              <ul className="chips">
                {SAMPLE.keywords.map((keyword) => (
                  <li className="chip" key={keyword}>{keyword}</li>
                ))}
              </ul>
            </div>

            <div className="hero-preview-section">
              <div className="hero-preview-heading">Skills coverage</div>
              <div className="hero-preview-coverage">
                <div className="bar">
                  <div className="bar-fill" style={{ width: `${SAMPLE.skillsCoverage}%` }} />
                </div>
                <strong>{SAMPLE.skillsCoverage}%</strong>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="how">
        <div className="how-head">
          <h2>How it works</h2>
          <p>Three simple steps to see where your resume stands</p>
        </div>
        <ol className="how-steps">
          {steps.map(({ icon: Icon, title, text }, index) => (
            <li className="how-step" key={title}>
              <span className="how-icon">
                <Icon size={28} />
              </span>
              <div>
                <span className="how-number">{String(index + 1).padStart(2, '0')}</span>
                <h3>{title}</h3>
                <p>{text}</p>
              </div>
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}