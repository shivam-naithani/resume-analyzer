import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="card empty-state card-narrow">
      <p className="not-found-code" aria-hidden="true">404</p>
      <h1>Page not found</h1>
      <p className="muted">The page you are looking for doesn&apos;t exist.</p>
      <Link to="/" className="btn btn-primary">Back to home</Link>
    </section>
  );
}