import api from './api';

export const signupRequest = (payload) => api.post('/auth/signup', payload).then((res) => res.data);

export const loginRequest = (credentials) => api.post('/auth/login', credentials).then((res) => res.data);