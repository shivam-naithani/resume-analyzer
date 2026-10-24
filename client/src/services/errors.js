// Turns an API error (the backend's ApiError JSON) into something the UI can show.
export function parseApiError(error) {
  if (!error.response) {
    return { message: 'Cannot reach the server. Check your connection and try again.', fieldErrors: {} };
  }

  const data = error.response.data;
  return {
    message: data?.message || 'Something went wrong. Please try again.',
    fieldErrors: data?.fieldErrors || {},
  };
}