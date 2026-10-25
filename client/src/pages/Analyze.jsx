import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FormField from '../components/FormField';
import { createAnalysis } from '../services/analyses';
import { parseApiError } from '../services/errors';
import { uploadResume } from '../services/resumes';

const MAX_FILE_BYTES = 5 * 1024 * 1024;
const MAX_DESCRIPTION_LENGTH = 20000;

function validateFile(file) {
  if (!file) return 'Choose a PDF resume to upload.';
  if (file.type !== 'application/pdf') return 'Only PDF files are supported.';
  if (file.size === 0) return 'This file is empty.';
  if (file.size > MAX_FILE_BYTES) return 'This file is larger than 5 MB.';
  return '';
}

const BUTTON_LABELS = {
  idle: 'Analyze resume',
  uploading: 'Uploading resume...',
  analyzing: 'Analyzing...',
};

export default function Analyze() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [fileError, setFileError] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [status, setStatus] = useState('idle');
  // The resume that was already uploaded for the selected file, so a retry doesn't upload it twice.
  const [uploaded, setUploaded] = useState(null);

  const handleFileChange = (event) => {
    const selected = event.target.files[0] ?? null;
    setFile(selected);
    setFileError(selected ? validateFile(selected) : '');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const problem = validateFile(file);
    if (problem) {
      setFileError(problem);
      return;
    }

    // "required" lets whitespace through, so check the trimmed text before uploading anything.
    const description = jobDescription.trim();
    if (!description) {
      setError('');
      setFieldErrors({ jobDescriptionText: 'Paste the job description you are applying for.' });
      return;
    }

    setError('');
    setFieldErrors({});

    try {
      let resume = uploaded && uploaded.file === file ? uploaded : null;
      if (!resume) {
        setStatus('uploading');
        const result = await uploadResume(file);
        resume = { file, id: result.id };
        setUploaded(resume);
      }

      setStatus('analyzing');
      const analysis = await createAnalysis({
        resumeId: resume.id,
        jobDescriptionText: description,
      });
      navigate(`/analyses/${analysis.id}`);
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.message);
      setFieldErrors(parsed.fieldErrors);
      setStatus('idle');
    }
  };

  return (
    <section className="analyze-card">
      <h1>Analyze your resume</h1>
      <p className="intro">
        Upload your resume and paste the job description you are applying for. We compare the two
        and show you how well they match.
      </p>

      {error && <p className="form-banner error" role="alert">{error}</p>}

      <form onSubmit={handleSubmit}>
        <FormField
          label="Resume (PDF, up to 5 MB)"
          name="resume"
          type="file"
          accept="application/pdf"
          onChange={handleFileChange}
          error={fileError}
        />
        <FormField
          as="textarea"
          label="Job description"
          name="jobDescriptionText"
          rows={10}
          required
          maxLength={MAX_DESCRIPTION_LENGTH}
          placeholder="Paste the full job description here"
          value={jobDescription}
          onChange={(event) => setJobDescription(event.target.value)}
          error={fieldErrors.jobDescriptionText}
        />
        <button type="submit" className="btn btn-primary" disabled={status !== 'idle'}>
          {BUTTON_LABELS[status]}
        </button>
      </form>
    </section>
  );
}