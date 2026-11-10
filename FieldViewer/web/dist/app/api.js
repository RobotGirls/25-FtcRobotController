/** Talks to the Java backend that runs the real PurePursuitFollower. */
async function postJson(url, body) {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    const data = (await response.json());
    if (!response.ok || data.error) {
        throw new Error(data.error ?? `${url} failed (${response.status})`);
    }
    return data;
}
export async function fetchRoutes() {
    const response = await fetch('/api/routes');
    if (!response.ok) {
        throw new Error(`couldn't load routes (${response.status})`);
    }
    return (await response.json());
}
export async function fetchDefaults() {
    const response = await fetch('/api/defaults');
    if (!response.ok) {
        throw new Error(`couldn't load tuning defaults (${response.status})`);
    }
    return (await response.json());
}
export async function fetchRecordings() {
    const response = await fetch('/api/recordings', { cache: 'no-store' });
    if (!response.ok) {
        throw new Error(`couldn't load recordings (${response.status})`);
    }
    return (await response.json());
}
export function saveRecording(recording) {
    return postJson('/api/recordings', recording);
}
export function simulateRoute(route, holonomic, tuning) {
    return postJson('/api/simulate', { route, holonomic, ...tuning });
}
export function simulateSketch(sketch, holonomic, tuning) {
    return postJson('/api/simulate', { sketch, holonomic, ...tuning });
}
export function connectLive(host) {
    return postJson('/api/live/connect', { host });
}
export function disconnectLive() {
    return postJson('/api/live/disconnect', {});
}
/** Subscribes to live robot frames and connection status. Returns a function that unsubscribes. */
export function subscribeLive(onFrame, onStatus) {
    const events = new EventSource('/api/live/events');
    events.addEventListener('frame', (e) => onFrame(JSON.parse(e.data)));
    events.addEventListener('status', (e) => onStatus(JSON.parse(e.data)));
    return () => events.close();
}
//# sourceMappingURL=api.js.map