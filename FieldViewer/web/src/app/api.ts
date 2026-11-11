import type { Defaults, LiveFrame, LiveStatus, Recording, RoutesResponse, SimResult, SketchRequest, Tuning } from './types.js';

/** Talks to the Java backend that runs the real PurePursuitFollower. */

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = (await response.json()) as T & { error?: string };
  if (!response.ok || data.error) {
    throw new Error(data.error ?? `${url} failed (${response.status})`);
  }
  return data;
}

export async function fetchRoutes(): Promise<RoutesResponse> {
  const response = await fetch('/api/routes');
  if (!response.ok) {
    throw new Error(`couldn't load routes (${response.status})`);
  }
  return (await response.json()) as RoutesResponse;
}

export async function fetchDefaults(): Promise<Defaults> {
  const response = await fetch('/api/defaults');
  if (!response.ok) {
    throw new Error(`couldn't load tuning defaults (${response.status})`);
  }
  return (await response.json()) as Defaults;
}

export interface SavedRecording {
  name: string;
  recording: Recording;
}

export interface RecordingIndex {
  directory: string;
  recordings: SavedRecording[];
}

export async function fetchRecordings(): Promise<RecordingIndex> {
  const response = await fetch('/api/recordings', { cache: 'no-store' });
  if (!response.ok) {
    throw new Error(`couldn't load recordings (${response.status})`);
  }
  return (await response.json()) as RecordingIndex;
}

export function saveRecording(recording: Recording): Promise<{ name: string; path: string }> {
  return postJson('/api/recordings', recording);
}

export function simulateRoute(route: string, holonomic: boolean, tuning: Tuning): Promise<SimResult> {
  return postJson<SimResult>('/api/simulate', { route, holonomic, ...tuning });
}

export function simulateSketch(sketch: SketchRequest, holonomic: boolean, tuning: Tuning): Promise<SimResult> {
  return postJson<SimResult>('/api/simulate', { sketch, holonomic, ...tuning });
}

export function connectLive(host: string): Promise<unknown> {
  return postJson('/api/live/connect', { host });
}

export function disconnectLive(): Promise<unknown> {
  return postJson('/api/live/disconnect', {});
}

export interface LimelightPipelineStatus {
  pipeline: number;
  minimum: number;
  maximum: number;
}

export async function fetchLimelightPipeline(): Promise<LimelightPipelineStatus> {
  const response = await fetch('/api/live/limelight/pipeline', { cache: 'no-store' });
  const data = (await response.json()) as LimelightPipelineStatus & { error?: string };
  if (!response.ok || data.error) {
    throw new Error(data.error ?? `pipeline read failed (${response.status})`);
  }
  return data;
}

export function setLimelightPipeline(pipeline: number): Promise<LimelightPipelineStatus> {
  return postJson('/api/live/limelight/pipeline', { pipeline });
}

export type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };
export type PipelineConfig = { [key: string]: JsonValue };

export async function fetchLimelightPipelineConfig(index: number): Promise<PipelineConfig> {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 12_000);
  let response: Response;
  try {
    response = await fetch(`/api/live/limelight/pipeline/config?index=${index}`, {
      cache: 'no-store', signal: controller.signal,
    });
  } catch (error) {
    if ((error as Error).name === 'AbortError') throw new Error('Limelight pipeline load timed out');
    throw error;
  } finally {
    window.clearTimeout(timeout);
  }
  const text = await response.text();
  let data: PipelineConfig & { error?: string };
  try {
    data = JSON.parse(text) as PipelineConfig & { error?: string };
  } catch {
    const videoResponse = text.trimStart().startsWith('--');
    throw new Error(videoResponse
      ? 'Received camera video instead of settings — deploy the updated Robot Controller app'
      : `Limelight returned invalid pipeline data (${response.status})`);
  }
  if (!response.ok || data.error) {
    throw new Error(data.error ?? `pipeline ${index} load failed (${response.status})`);
  }
  return data;
}

export function saveLimelightPipelineConfig(index: number, config: PipelineConfig): Promise<{ ok: boolean; pipeline: number }> {
  return postJson(`/api/live/limelight/pipeline/config?index=${index}`, config);
}

/** Subscribes to live robot frames and connection status. Returns a function that unsubscribes. */
export function subscribeLive(
  onFrame: (frame: LiveFrame) => void,
  onStatus: (status: LiveStatus) => void,
): () => void {
  const events = new EventSource('/api/live/events');
  events.addEventListener('frame', (e) => onFrame(JSON.parse((e as MessageEvent<string>).data) as LiveFrame));
  events.addEventListener('status', (e) => onStatus(JSON.parse((e as MessageEvent<string>).data) as LiveStatus));
  return () => events.close();
}
