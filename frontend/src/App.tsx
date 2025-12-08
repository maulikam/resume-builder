import { useEffect, useState } from 'react';
import { aiGapFill, aiRewrite, aiSuggest, aiSummarize, atsGap, atsPreview, atsScore, downloadCoverLetter, downloadResume, generateCoverLetter, generateResume, listJobDescriptions, listProfiles, login, me } from './api';

type Profile = { id: number; fullName: string; email: string };
type JobDescription = { id: number; title: string; company: string };

function App() {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('password');
  const [authStatus, setAuthStatus] = useState<string>('Not logged in');
  const [profiles, setProfiles] = useState<Profile[]>([]);
  const [jds, setJds] = useState<JobDescription[]>([]);
  const [profileQuery, setProfileQuery] = useState('');
  const [jdQuery, setJdQuery] = useState('');
  const [profilePage, setProfilePage] = useState(0);
  const [jdPage, setJdPage] = useState(0);
  const [selectedProfile, setSelectedProfile] = useState<number | null>(null);
  const [selectedJd, setSelectedJd] = useState<number | null>(null);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [atsResult, setAtsResult] = useState<any>(null);
  const [atsMissing, setAtsMissing] = useState<string[]>([]);
  const [aiOutput, setAiOutput] = useState<string>('');
  const [previewText, setPreviewText] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [resumeGen, setResumeGen] = useState<{ id?: number; status?: string; url?: string }>({});
  const [coverGen, setCoverGen] = useState<{ id?: number; status?: string; url?: string }>({});

  const handleLogin = async () => {
    setLoading(true);
    try {
      await login(username, password);
      const who = await me();
      setAuthStatus(`Logged in as ${who.username} [${who.roles.join(', ')}]`);
      loadData();
    } catch {
      setAuthStatus('Login failed');
    } finally {
      setLoading(false);
    }
  };

  const doGenerateResume = async () => {
    if (!selectedProfile || !selectedJd) return;
    setLoading(true);
    try {
      const res = await generateResume(selectedProfile, selectedJd);
      setResumeGen({ id: res.generatedResumeId, status: res.status });
      if (res.generatedResumeId) {
        const url = await downloadResume(res.generatedResumeId);
        setResumeGen({ id: res.generatedResumeId, status: res.status, url });
      }
    } catch (e) {
      setResumeGen({ status: 'failed' });
    } finally {
      setLoading(false);
    }
  };

  const doGenerateCover = async () => {
    if (!selectedProfile || !selectedJd) return;
    setLoading(true);
    try {
      const res = await generateCoverLetter(selectedProfile, selectedJd);
      setCoverGen({ id: res.generatedId, status: res.status });
      if (res.generatedId) {
        const url = await downloadCoverLetter(res.generatedId);
        setCoverGen({ id: res.generatedId, status: res.status, url });
      }
    } catch (e) {
      setCoverGen({ status: 'failed' });
    } finally {
      setLoading(false);
    }
  };

  const loadData = async () => {
    try {
      const p = await listProfiles(profilePage, 10, profileQuery);
      const j = await listJobDescriptions(jdPage, 10, jdQuery);
      setProfiles(p.content ?? []);
      setJds(j.content ?? []);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    // Optional: auto-login if dev creds work
  }, []);

  const runAts = async () => {
    if (!selectedProfile || !selectedJd) return;
    setLoading(true);
    try {
      const score = await atsScore(selectedProfile, selectedJd, selectedResumeId ?? undefined);
      const missing = await atsGap(selectedProfile, selectedJd, selectedResumeId ?? undefined);
      const preview = await atsPreview(selectedProfile, selectedResumeId ?? undefined);
      setAtsResult(score);
      setAtsMissing(missing as string[]);
      setPreviewText(preview);
    } finally {
      setLoading(false);
    }
  };

  const runAi = async () => {
    if (!selectedProfile || !selectedJd) return;
    setLoading(true);
    try {
      const res = await aiSuggest(selectedProfile, selectedJd);
      setAiOutput(res.suggestions ?? '');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: '1rem', maxWidth: 1200, margin: '0 auto' }}>
      <h1>Resume Builder Dashboard</h1>
      <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))' }}>
        <section style={cardStyle}>
          <h2>Auth</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <input placeholder="username" value={username} onChange={e => setUsername(e.target.value)} />
            <input placeholder="password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
            <button onClick={handleLogin} disabled={loading}>Login</button>
            <div>{authStatus}</div>
          </div>
        </section>

        <section style={cardStyle}>
          <h2>Profiles</h2>
          <input placeholder="search" value={profileQuery} onChange={e => setProfileQuery(e.target.value)} />
          <select value={selectedProfile ?? ''} onChange={e => setSelectedProfile(Number(e.target.value))}>
            <option value="">Select profile</option>
            {profiles.map(p => (
              <option key={p.id} value={p.id}>{p.fullName} ({p.email})</option>
            ))}
          </select>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
            <button onClick={() => { setProfilePage(Math.max(profilePage - 1, 0)); loadData(); }}>Prev</button>
            <button onClick={() => { setProfilePage(profilePage + 1); loadData(); }}>Next</button>
            <button onClick={loadData}>Refresh</button>
          </div>
        </section>

        <section style={cardStyle}>
          <h2>Job Descriptions</h2>
          <input placeholder="search" value={jdQuery} onChange={e => setJdQuery(e.target.value)} />
          <select value={selectedJd ?? ''} onChange={e => setSelectedJd(Number(e.target.value))}>
            <option value="">Select JD</option>
            {jds.map(j => (
              <option key={j.id} value={j.id}>{j.title} @ {j.company}</option>
            ))}
          </select>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
            <button onClick={() => { setJdPage(Math.max(jdPage - 1, 0)); loadData(); }}>Prev</button>
            <button onClick={() => { setJdPage(jdPage + 1); loadData(); }}>Next</button>
            <button onClick={loadData}>Refresh</button>
          </div>
        </section>
      </div>

      <div style={{ display: 'grid', gap: '1rem', marginTop: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))' }}>
        <section style={cardStyle}>
          <h2>ATS Score</h2>
          <input placeholder="Resume ID (optional)" value={selectedResumeId ?? ''} onChange={e => setSelectedResumeId(e.target.value ? Number(e.target.value) : null)} />
          <button onClick={runAts} disabled={loading || !selectedProfile || !selectedJd}>Run ATS</button>
          {atsResult && (
            <div style={{ marginTop: '0.5rem' }}>
              <div><strong>Score:</strong> {atsResult.score}%</div>
              <div><strong>Source:</strong> {atsResult.source}</div>
              {atsResult.sectionCoverage && (
                <ul>
                  <li>Summary: {(atsResult.sectionCoverage.summary * 100).toFixed(0)}%</li>
                  <li>Experience: {(atsResult.sectionCoverage.experience * 100).toFixed(0)}%</li>
                  <li>Skills: {(atsResult.sectionCoverage.skills * 100).toFixed(0)}%</li>
                  <li>Education: {(atsResult.sectionCoverage.education * 100).toFixed(0)}%</li>
                </ul>
              )}
              <div style={{ marginTop: '0.5rem' }}>
                Missing keywords: {atsMissing.join(', ') || 'None'}
              </div>
            </div>
          )}
        </section>

        <section style={cardStyle}>
          <h2>AI Suggestions</h2>
          <button onClick={runAi} disabled={loading || !selectedProfile || !selectedJd}>Suggest Bullets</button>
          <pre style={{ whiteSpace: 'pre-wrap' }}>{aiOutput}</pre>
          <div style={{ marginTop: '0.5rem' }}>
            <button onClick={async () => {
              if (!selectedProfile) return;
              setLoading(true);
              try {
                const prof = profiles.find(p => p.id === selectedProfile);
                const summary = prof ? await aiSummarize(`${prof.fullName} ${prof.email}`) : { suggestions: '' };
                setAiOutput(summary.suggestions ?? '');
              } finally {
                setLoading(false);
              }
            }}>Summarize Profile</button>
            <button onClick={async () => {
              const bullet = prompt('Enter bullet to rewrite');
              const jd = jds.find(j => j.id === selectedJd);
              if (!bullet || !jd) return;
              setLoading(true);
              try {
                const res = await aiRewrite(bullet, `${jd.title} ${jd.company}`);
                setAiOutput(res.suggestions ?? '');
              } finally {
                setLoading(false);
              }
            }}>Rewrite Bullet</button>
            <button onClick={async () => {
              if (!atsMissing.length) return;
              setLoading(true);
              try {
                const res = await aiGapFill(atsMissing.join(', '), previewText);
                setAiOutput(res.suggestions ?? '');
              } finally {
                setLoading(false);
              }
            }} disabled={!atsMissing.length}>Gap Fill</button>
          </div>
        </section>

        <section style={cardStyle}>
          <h2>ATS Preview</h2>
          <pre style={{ whiteSpace: 'pre-wrap', maxHeight: 240, overflow: 'auto' }}>{previewText}</pre>
        </section>

        <section style={cardStyle}>
          <h2>Generate</h2>
          <button disabled={!selectedProfile || !selectedJd || loading} onClick={doGenerateResume}>Generate Resume</button>
          {resumeGen.status && <div>Status: {resumeGen.status}</div>}
          {resumeGen.url && <a href={resumeGen.url} download={`resume-${resumeGen.id}.pdf`}>Download Resume</a>}
          <hr />
          <button disabled={!selectedProfile || !selectedJd || loading} onClick={doGenerateCover}>Generate Cover Letter</button>
          {coverGen.status && <div>Status: {coverGen.status}</div>}
          {coverGen.url && <a href={coverGen.url} download={`cover-letter-${coverGen.id}.pdf`}>Download Cover Letter</a>}
        </section>
      </div>
    </div>
  );
}

const cardStyle: React.CSSProperties = {
  border: '1px solid #ccc',
  borderRadius: 8,
  padding: '1rem',
  boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
};

export default App;
