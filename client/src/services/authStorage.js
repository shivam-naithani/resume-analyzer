const TOKEN_KEY = 'token';
const USER_KEY = 'user';

export const getToken = () => localStorage.getItem(TOKEN_KEY);

export function saveSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// Reads the "exp" claim from the JWT payload. A token that can't be read counts as expired.
function isTokenExpired(token) {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const { exp } = JSON.parse(atob(payload));
    return typeof exp !== 'number' || exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

// Restores the logged-in session after a page refresh, or returns null if there isn't a valid one.
export function loadSession() {
  const token = getToken();
  const rawUser = localStorage.getItem(USER_KEY);

  if (!token || !rawUser || isTokenExpired(token)) {
    clearSession();
    return null;
  }

  try {
    return { token, user: JSON.parse(rawUser) };
  } catch {
    clearSession();
    return null;
  }
}