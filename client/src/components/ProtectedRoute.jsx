import { useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function ProtectedRoute() {
  const { isAuthenticated, logoutReason } = useAuth();
  const location = useLocation();
  // Was the user logged in when this route first rendered? If so, a missing session now
  // means it ended while they were on the page, and we know why.
  const [wasLoggedIn] = useState(isAuthenticated);

  if (isAuthenticated) {
    return <Outlet />;
  }

  if (wasLoggedIn && logoutReason === 'user') {
    return <Navigate to="/" replace />;
  }

  const message =
    wasLoggedIn && logoutReason === 'expired'
      ? 'Your session has expired. Please log in again.'
      : 'Please log in to continue.';

  return <Navigate to="/login" replace state={{ from: location, message }} />;
}