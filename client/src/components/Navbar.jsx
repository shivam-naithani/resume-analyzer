import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { CloseIcon, LogoIcon, MenuIcon } from './icons';

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
          <LogoIcon size={26} />
          <span>Resume <span className="brand-accent">Analyzer</span></span>
        </Link>

        <button
          type="button"
          className="menu-toggle"
          aria-label="Toggle navigation menu"
          aria-expanded={open}
          aria-controls="site-nav"
          onClick={() => setOpen((value) => !value)}
        >
          {open ? <CloseIcon size={22} /> : <MenuIcon size={22} />}
        </button>

        <nav id="site-nav" className={open ? 'nav nav-open' : 'nav'}>
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
              <NavLink to="/login" className="nav-login" onClick={close}>Log in</NavLink>
              <NavLink to="/signup" className="btn btn-primary" onClick={close}>Sign up</NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}