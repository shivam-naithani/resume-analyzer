import api from './api';

export const createAnalysis = (payload) => api.post('/analyses', payload).then((res) => res.data);

export const getAnalysis = (id) => api.get(`/analyses/${id}`).then((res) => res.data);