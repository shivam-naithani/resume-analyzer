import api from './api';

export const createAnalysis = (payload) => api.post('/analyses', payload).then((res) => res.data);

export const getAnalysis = (id) => api.get(`/analyses/${id}`).then((res) => res.data);

export const listAnalyses = () => api.get('/analyses').then((res) => res.data);

export const deleteAnalysis = (id) => api.delete(`/analyses/${id}`).then(() => undefined);