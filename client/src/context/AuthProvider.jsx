import { useCallback, useEffect, useMemo, useState } from 'react';
import { AuthContext } from './AuthContext';
import { setUnauthorizedHandler } from '../services/api';
import { loginRequest, signupRequest } from '../services/auth';
import { clearSession, loadSession, saveSession } from '../services/authStorage';

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession);
  // Why the last session ended: 'user' (clicked log out) or 'expired' (the server rejected the token).
  const [logoutReason, setLogoutReason] = useState(null);

  const endSession = useCallback((reason) => {
    clearSession();
    setSession(null);
    setLogoutReason(reason);
  }, []);

  const logout = useCallback(() => endSession('user'), [endSession]);

  useEffect(() => {
    setUnauthorizedHandler(() => endSession('expired'));
    return () => setUnauthorizedHandler(null);
  }, [endSession]);

  const login = useCallback(async (email, password) => {
    const { token, user } = await loginRequest({ email, password });
    saveSession(token, user);
    setSession({ token, user });
    setLogoutReason(null);
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
      logoutReason,
      login,
      signup,
      logout,
    }),
    [session, logoutReason, login, signup, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}