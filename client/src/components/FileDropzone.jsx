import { useRef, useState } from 'react';
import { formatFileSize } from '../utils/formatFileSize';
import { FileIcon, UploadIcon } from './icons';

export default function FileDropzone({
  name,
  label,
  hint,
  accept,
  file,
  error,
  disabled = false,
  onSelect,
}) {
  const inputRef = useRef(null);
  const [dragging, setDragging] = useState(false);
  const hintId = `${name}-hint`;
  const errorId = `${name}-error`;
  const describedBy = [hint ? hintId : null, error ? errorId : null].filter(Boolean).join(' ') || undefined;

  const openPicker = () => inputRef.current?.click();

  // Cancelling the picker can report an empty selection; keep the current file in that case.
  const handleChange = (event) => {
    const selected = event.target.files[0];
    if (selected) onSelect(selected);
  };

  const handleRemove = () => {
    // Clear the native input too, so choosing the same file again still fires a change event.
    if (inputRef.current) inputRef.current.value = '';
    onSelect(null);
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    if (!disabled) setDragging(true);
  };

  const handleDragLeave = (event) => {
    if (!event.currentTarget.contains(event.relatedTarget)) setDragging(false);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setDragging(false);
    if (disabled) return;
    const dropped = event.dataTransfer.files?.[0];
    if (dropped) onSelect(dropped);
  };

  const classes = ['dropzone', dragging && 'dragging', file && 'has-file', error && 'invalid']
    .filter(Boolean)
    .join(' ');

  return (
    <div className="form-field">
      <label htmlFor={name}>{label}</label>
      <div
        className={classes}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        <input ref={inputRef} type="file" hidden accept={accept} onChange={handleChange} />

        {file ? (
          <div className="dropzone-file">
            <span className="dropzone-file-icon">
              <FileIcon size={24} />
            </span>
            <span className="dropzone-file-info">
              <span className="dropzone-file-name">{file.name}</span>
              <span className="muted small">{formatFileSize(file.size)}</span>
            </span>
            <span className="dropzone-actions">
              <button
                type="button"
                id={name}
                className="dropzone-action"
                aria-describedby={describedBy}
                disabled={disabled}
                onClick={openPicker}
              >
                Change
              </button>
              <button type="button" className="dropzone-action" disabled={disabled} onClick={handleRemove}>
                Remove
              </button>
            </span>
          </div>
        ) : (
          <button
            type="button"
            id={name}
            className="dropzone-prompt"
            aria-describedby={describedBy}
            disabled={disabled}
            onClick={openPicker}
          >
            <span className="dropzone-icon">
              <UploadIcon size={28} />
            </span>
            <span className="dropzone-title">
              Drop your PDF here or <span>browse</span>
            </span>
          </button>
        )}
      </div>
      {hint && (
        <p className="form-hint" id={hintId}>
          {hint}
        </p>
      )}
      {error && (
        <p className="form-error" id={errorId}>
          {error}
        </p>
      )}
    </div>
  );
}