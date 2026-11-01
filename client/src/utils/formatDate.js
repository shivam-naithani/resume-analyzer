// Formats an API timestamp for display, or returns null if it can't be read.
export function formatDateTime(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? null
    : date.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}