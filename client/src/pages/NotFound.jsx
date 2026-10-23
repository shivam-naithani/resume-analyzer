import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="page">
      <h1>Page not found</h1>
      <p>The page you are looking for doesn&apos;t exist.</p>
      <Link to="/" className="btn btn-primary">Back to home</Link>
    </section>
  );
}