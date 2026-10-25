import api from './api';

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/resumes', formData).then((res) => res.data);
};