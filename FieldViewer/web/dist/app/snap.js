const HALF_FIELD = 72;
const GRID = 12;
/** Lower is preferred when two targets are about as close. */
const Priority = { Neighbor: 0, Other: 1, Wall: 2, Grid: 3 };
export function snapPoint(raw, options) {
    const { reach } = options;
    const x = best(raw[0], candidates(0, options), reach);
    const y = best(raw[1], candidates(1, options), reach);
    let point = [x ? x.value : Math.round(raw[0]), y ? y.value : Math.round(raw[1])];
    let diagonal = null;
    if (!x || !y) {
        const hit = snapDiagonal(raw, x?.value ?? null, y?.value ?? null, options);
        if (hit) {
            point = hit.point;
            diagonal = hit.guide;
        }
    }
    point = [clamp(point[0]), clamp(point[1])];
    const guides = [];
    if (x)
        guides.push(guideFor(0, x, point));
    if (y)
        guides.push(guideFor(1, y, point));
    if (diagonal)
        guides.push({ ...diagonal, b: point, at: point });
    return { point, guides };
}
/** No snapping (snap off, or Option/Alt held): just a tenth of an inch. */
export function freePoint(raw) {
    return [clamp(Math.round(raw[0] * 10) / 10), clamp(Math.round(raw[1] * 10) / 10)];
}
function candidates(axis, { neighbors, others, robotSize }) {
    const out = [];
    for (const p of neighbors)
        out.push({ value: p[axis], priority: Priority.Neighbor, from: p });
    for (const p of others)
        out.push({ value: p[axis], priority: Priority.Other, from: p });
    // robot center when its side is against a wall (either way round)
    for (const half of new Set(robotSize.map((s) => s / 2))) {
        for (const sign of [-1, 1]) {
            out.push({ value: sign * (HALF_FIELD - half), priority: Priority.Wall, from: null, label: 'against wall' });
        }
    }
    for (let v = -HALF_FIELD; v <= HALF_FIELD; v += GRID) {
        out.push({ value: v, priority: Priority.Grid, from: null });
    }
    return out;
}
function best(value, options, reach) {
    let pick = null;
    let score = Infinity;
    for (const c of options) {
        const distance = Math.abs(c.value - value);
        if (distance > reach)
            continue;
        // a better kind of target wins unless it is clearly further away
        const s = distance + c.priority * reach * 0.2;
        if (s < score) {
            score = s;
            pick = c;
        }
    }
    return pick;
}
/** 45° lines through the neighbors; meets an axis that already snapped, or projects onto the line. */
function snapDiagonal(raw, x, y, { neighbors, reach }) {
    let found = null;
    let closest = reach;
    for (const q of neighbors) {
        for (const slope of [1, -1]) {
            let point;
            if (x !== null) {
                point = [x, q[1] + slope * (x - q[0])];
            }
            else if (y !== null) {
                point = [q[0] + slope * (y - q[1]), y];
            }
            else {
                // whole inches along the line keeps it exactly 45° from a whole-inch neighbor
                const s = Math.round(((raw[0] - q[0]) + slope * (raw[1] - q[1])) / 2);
                point = [q[0] + s, q[1] + slope * s];
            }
            const distance = Math.hypot(point[0] - raw[0], point[1] - raw[1]);
            if (distance < closest && Math.hypot(point[0] - q[0], point[1] - q[1]) > 1) {
                closest = distance;
                found = { point, guide: { a: q, b: point, label: '45°', at: point } };
            }
        }
    }
    return found;
}
function guideFor(axis, c, point) {
    if (c.from) {
        return { a: c.from, b: point, at: point };
    }
    // a line across the whole field
    const a = axis === 0 ? [c.value, -HALF_FIELD] : [-HALF_FIELD, c.value];
    const b = axis === 0 ? [c.value, HALF_FIELD] : [HALF_FIELD, c.value];
    return { a, b, label: c.label, at: point };
}
function clamp(v) {
    return Math.max(-HALF_FIELD, Math.min(HALF_FIELD, v));
}
//# sourceMappingURL=snap.js.map