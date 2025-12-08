export type Tokens = { accessToken: string; refreshToken: string; expiresIn: number; tokenType: string };
export type Profile = { id: number; fullName: string; email: string };
export type JobDescription = { id: number; title: string; company: string };
export type Page<T> = { content?: T[]; totalElements?: number; totalPages?: number; number?: number; size?: number };
export type AtsSectionCoverage = { summary: number; experience: number; skills: number; education: number };
export type AtsScoreResponse = { score: number; missingKeywords: string[]; preview: string; source: string; sectionCoverage: AtsSectionCoverage };
export type ResumeGenerationResponse = { generatedResumeId?: number; status?: string; message?: string };
export type CoverLetterResponse = { generatedId?: number; status?: string; message?: string };

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

let accessToken: string | null = null;
let refreshToken: string | null = null;

export function setTokens(tokens: Tokens) {
  accessToken = tokens.accessToken;
  refreshToken = tokens.refreshToken;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
  };
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (res.status === 401 && refreshToken) {
    const refreshed = await refresh();
    if (refreshed) {
      return request<T>(path, options);
    }
  }
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  if (res.status === 204) {
    return {} as T;
  }
  return res.json();
}

export async function login(username: string, password: string): Promise<Tokens> {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) throw new Error('Login failed');
  const data = (await res.json()) as Tokens;
  setTokens(data);
  return data;
}

async function refresh(): Promise<boolean> {
  if (!refreshToken) return false;
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!res.ok) return false;
  const data = (await res.json()) as Tokens;
  setTokens(data);
  return true;
}

export async function me(): Promise<{ username: string; roles: string[] }> {
  return request('/api/auth/me');
}

export async function listProfiles(page = 0, size = 10, q = ''): Promise<Page<Profile>> {
  const search = q ? `&q=${encodeURIComponent(q)}` : '';
  return request(`/api/profiles?page=${page}&size=${size}${search}`);
}

export async function listJobDescriptions(page = 0, size = 10, q = ''): Promise<Page<JobDescription>> {
  const search = q ? `&q=${encodeURIComponent(q)}` : '';
  return request(`/api/job-descriptions?page=${page}&size=${size}${search}`);
}

export async function atsScore(profileId: number, jdId: number, resumeId?: number): Promise<AtsScoreResponse> {
  const resume = resumeId ? `&resumeId=${resumeId}` : '';
  return request(`/api/ats/score/profile/${profileId}/jd/${jdId}?${resume}`);
}

export async function atsGap(profileId: number, jdId: number, resumeId?: number): Promise<string[]> {
  const resume = resumeId ? `&resumeId=${resumeId}` : '';
  return request(`/api/ats/gap/profile/${profileId}/jd/${jdId}?${resume}`);
}

export async function atsPreview(profileId: number, resumeId?: number): Promise<string> {
  const resume = resumeId ? `?resumeId=${resumeId}` : '';
  const res = await fetch(`${API_BASE}/api/ats/preview/profile/${profileId}${resume}`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  });
  if (!res.ok) throw new Error('Preview failed');
  return res.text();
}

export async function aiSuggest(profileId: number, jdId: number): Promise<{ suggestions: string }> {
  return request('/api/ai/suggest-bullets', {
    method: 'POST',
    body: JSON.stringify({ profileId, jobDescriptionId: jdId }),
  });
}

export async function aiSummarize(text: string): Promise<{ suggestions: string }> {
  return request('/api/ai/summarize-profile', {
    method: 'POST',
    body: JSON.stringify({ text, instruction: 'Summarize profile' })
  });
}

export async function aiRewrite(bullet: string, jd: string): Promise<{ suggestions: string }> {
  return request('/api/ai/rewrite-bullet', {
    method: 'POST',
    body: JSON.stringify({ text: bullet, instruction: jd })
  });
}

export async function aiGapFill(missingKeywords: string, context: string): Promise<{ suggestions: string }> {
  return request('/api/ai/gap-fill', {
    method: 'POST',
    body: JSON.stringify({ text: context, instruction: missingKeywords })
  });
}

export async function generateResume(profileId: number, jobDescriptionId: number, templateId?: number): Promise<ResumeGenerationResponse> {
  return request('/api/generation/resume', {
    method: 'POST',
    body: JSON.stringify({ profileId, jobDescriptionId, templateId })
  });
}

export async function generateCoverLetter(profileId: number, jobDescriptionId: number, templateId?: number): Promise<CoverLetterResponse> {
  return request('/api/cover-letters', {
    method: 'POST',
    body: JSON.stringify({ profileId, jobDescriptionId, templateId })
  });
}

export async function downloadResume(generatedId: number) {
  const res = await fetch(`${API_BASE}/api/generation/resume/${generatedId}/download`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  });
  if (!res.ok) throw new Error('Download failed');
  const blob = await res.blob();
  return URL.createObjectURL(blob);
}

export async function downloadCoverLetter(generatedId: number) {
  const res = await fetch(`${API_BASE}/api/cover-letters/${generatedId}/download`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  });
  if (!res.ok) throw new Error('Download failed');
  const blob = await res.blob();
  return URL.createObjectURL(blob);
}
