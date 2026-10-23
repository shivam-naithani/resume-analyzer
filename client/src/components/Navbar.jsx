import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const close = () => setOpen(false);

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand" onClick={close}>
          Resume Analyzer
        </Link>

        <button
          type="button"
          className="menu-toggle"
          aria-label="Toggle navigation menu"
          aria-expanded={open}
          onClick={() => setOpen((value) => !value)}
        >
          {open ? '\u2715' : '\u2630'}
        </button>

        <nav className={open ? 'nav nav-open' : 'nav'}>
          <NavLink to="/analyze" onClick={close}>Analyze</NavLink>
          <NavLink to="/history" onClick={close}>History</NavLink>
          <span className="nav-spacer" />
          <NavLink to="/login" onClick={close}>Log in</NavLink>
          <NavLink to="/signup" className="btn btn-primary" onClick={close}>Sign up</NavLink>
        </nav>
      </div>
    </header>
  );
}