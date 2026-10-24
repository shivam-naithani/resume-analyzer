import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from './AuthContext';
import { setUnauthorizedHandler } from '../services/api';
import { loginRequest, signupRequest } from '../services/auth';
import { clearSession, loadSession, saveSession } from '../services/authStorage';

export default function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [session, setSession] = useState(loadSession);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
      navigate('/login', {
        replace: true,
        state: { message: 'Your session has expired. Please log in again.' },
      });
    });
    return () => setUnauthorizedHandler(null);
  }, [logout, navigate]);

  const login = useCallback(async (email, password) => {
    const { token, user } = await loginRequest({ email, password });
    saveSession(token, user);
    setSession({ token, user });
  }, []);

  // Signup doesn't return a token, so log in with the same credentials afterwards.
  const signup = useCallback(
    async (name, email, password) => {
      await signupRequest({ name, email, password });
      await login(email, password);
    },
    [login],
  );

  const value = useMemo(
    () => ({
      user: session?.user ?? null,
      isAuthenticated: session !== null,
      login,
      signup,
      logout,
    }),
    [session, login, signup, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}