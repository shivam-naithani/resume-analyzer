import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { CloseIcon, LogoIcon, MenuIcon, MoonIcon, SunIcon } from './icons';

const themeStorageKey = 'resume-analyzer-theme';

function getInitialTheme() {
  try {
    const savedTheme = window.localStorage.getItem(themeStorageKey);
    if (savedTheme === 'light' || savedTheme === 'dark') return savedTheme;
  } catch {
    // Continue with the system preference when storage is unavailable.
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const [theme, setTheme] = useState(getInitialTheme);
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const close = () => setOpen(false);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      window.localStorage.setItem(themeStorageKey, theme);
    } catch {
      // The selected theme still applies for this page when storage is unavailable.
    }
  }, [theme]);

  const toggleTheme = () => setTheme((value) => (value === 'dark' ? 'light' : 'dark'));

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
          <button
            type="button"
            className="theme-toggle"
            aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
            aria-pressed={theme === 'dark'}
            title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
            onClick={toggleTheme}
          >
            {theme === 'dark' ? <SunIcon size={19} /> : <MoonIcon size={19} />}
          </button>
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