import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const close = () => setOpen(false);

  const handleLogout = () => {
    close();
    logout();
    navigate('/');
  };

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
          {isAuthenticated ? (
            <>
              <span className="nav-user">{user.name}</span>
              <button type="button" className="btn btn-secondary" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" onClick={close}>Log in</NavLink>
              <NavLink to="/signup" className="btn btn-primary" onClick={close}>Sign up</NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}