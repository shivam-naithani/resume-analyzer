import axios from 'axios';
import { getToken } from './authStorage';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

let unauthorizedHandler = null;

// The auth provider registers a function here that logs the user out and sends them to the login page.
export const setUnauthorizedHandler = (handler) => {
  unauthorizedHandler = handler;
};

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const isAuthRequest = (error.config?.url ?? '').startsWith('/auth/');

    // A 401 on a normal request while logged in means the token expired or was rejected.
    // A 401 from /auth/login just means wrong credentials, so it must not log anyone out.
    if (status === 401 && !isAuthRequest && getToken() && unauthorizedHandler) {
      unauthorizedHandler();
    }
    return Promise.reject(error);
  },
);

export const checkHealth = () => api.get('/health').then((res) => res.data);

export default api;