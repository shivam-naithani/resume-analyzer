import { Link } from 'react-router-dom';

const steps = [
  { title: 'Upload your resume', text: 'Add your resume as a PDF. Your text is extracted for analysis.' },
  { title: 'Paste a job description', text: 'Choose the role you are applying for and paste its description.' },
  { title: 'Review your results', text: 'See your match score, missing keywords, a section breakdown and suggestions.' },
];

export default function Home() {
  return (
    <>
      <section className="hero">
        <h1>Know how your resume matches the job before you apply</h1>
        <p>
          Get a match score, the keywords you are missing, a section-by-section breakdown and
          concrete suggestions, so your resume gets past automated screening.
        </p>
        <div className="hero-actions">
          <Link to="/analyze" className="btn btn-primary">Analyze my resume</Link>
          <Link to="/signup" className="btn btn-secondary">Create an account</Link>
        </div>
      </section>

      <section className="steps">
        {steps.map((step, index) => (
          <div className="step-card" key={step.title}>
            <span className="step-number">{index + 1}</span>
            <h3>{step.title}</h3>
            <p>{step.text}</p>
          </div>
        ))}
      </section>
    </>
  );
}