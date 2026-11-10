/** Drive powers and speed over time, under the field. */
export function emptySeries() {
    return { t: [], fwd: [], left: [], turn: [], speed: [] };
}
const LINES = [
    { key: 'fwd', label: 'Forward', color: '#b68cff' },
    { key: 'left', label: 'Strafe', color: '#f472b6' },
    { key: 'turn', label: 'Turn', color: '#a78bfa' },
];
const SPEED_COLOR = '#fbbf24';
const FONT = 'Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", sans-serif';
const LEFT = 46;
const RIGHT = 14;
export class TelemetryChart {
    canvas;
    /** Called with a time when the chart is clicked (simulation only). */
    onSeek = () => { };
    ctx;
    width = 0;
    height = 0;
    hoverX = null;
    last = null;
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        new ResizeObserver(() => {
            this.resize();
            this.redraw();
        }).observe(canvas.parentElement ?? canvas);
        this.resize();
        canvas.addEventListener('mousemove', (e) => {
            this.hoverX = e.offsetX;
            this.redraw();
        });
        canvas.addEventListener('mouseleave', () => {
            this.hoverX = null;
            this.redraw();
        });
        canvas.addEventListener('click', (e) => {
            if (this.last?.seekable) {
                this.onSeek(this.timeAt(e.offsetX, this.last.range));
            }
        });
    }
    resize() {
        const parent = this.canvas.parentElement ?? this.canvas;
        this.width = parent.clientWidth;
        this.height = parent.clientHeight;
        const dpr = window.devicePixelRatio || 1;
        this.canvas.style.width = `${this.width}px`;
        this.canvas.style.height = `${this.height}px`;
        this.canvas.width = Math.round(this.width * dpr);
        this.canvas.height = Math.round(this.height * dpr);
        this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    timeAt(x, [t0, t1]) {
        const plotWidth = Math.max(1, this.width - LEFT - RIGHT);
        return t0 + Math.max(0, Math.min(1, (x - LEFT) / plotWidth)) * (t1 - t0);
    }
    redraw() {
        if (this.last) {
            this.draw(this.last.series, this.last.cursor, this.last.range, this.last.seekable);
        }
    }
    draw(series, cursor, range, seekable) {
        this.last = { series, cursor, range, seekable };
        const { ctx, width, height } = this;
        ctx.clearRect(0, 0, width, height);
        this.canvas.style.cursor = seekable ? 'pointer' : 'default';
        if (width < 50 || height < 40)
            return;
        const [t0, t1] = range;
        const span = Math.max(1e-6, t1 - t0);
        const plotWidth = width - LEFT - RIGHT;
        const x = (t) => LEFT + ((t - t0) / span) * plotWidth;
        const gap = 10;
        const powerTop = 18;
        const powerHeight = (height - powerTop - gap - 18) * 0.55;
        const speedTop = powerTop + powerHeight + gap;
        const speedHeight = height - speedTop - 16;
        const maxSpeed = Math.max(20, Math.ceil(Math.max(0, ...series.speed) / 10) * 10);
        const yPower = (v) => powerTop + ((1 - v) / 2) * powerHeight;
        const ySpeed = (v) => speedTop + (1 - v / maxSpeed) * speedHeight;
        // axes and grid
        ctx.font = `500 10px ${FONT}`;
        ctx.textBaseline = 'middle';
        ctx.textAlign = 'right';
        ctx.lineWidth = 1;
        for (const v of [1, 0, -1]) {
            ctx.strokeStyle = v === 0 ? 'rgba(255, 255, 255, 0.14)' : 'rgba(255, 255, 255, 0.05)';
            line(ctx, LEFT, yPower(v), width - RIGHT, yPower(v));
            ctx.fillStyle = '#5b6675';
            ctx.fillText(v.toFixed(0), LEFT - 8, yPower(v));
        }
        for (const v of [0, maxSpeed / 2, maxSpeed]) {
            ctx.strokeStyle = v === 0 ? 'rgba(255, 255, 255, 0.14)' : 'rgba(255, 255, 255, 0.05)';
            line(ctx, LEFT, ySpeed(v), width - RIGHT, ySpeed(v));
            ctx.fillStyle = '#5b6675';
            ctx.fillText(v.toFixed(0), LEFT - 8, ySpeed(v));
        }
        ctx.textAlign = 'left';
        ctx.fillStyle = '#8b98a8';
        ctx.font = `650 10px ${FONT}`;
        ctx.fillText('POWER', LEFT, 8);
        ctx.fillText('SPEED in/s', LEFT, speedTop - 5);
        // legend
        let lx = width - RIGHT;
        ctx.textAlign = 'right';
        for (const item of [...LINES, { label: 'Speed', color: SPEED_COLOR }].reverse()) {
            ctx.fillStyle = '#8b98a8';
            ctx.font = `500 10px ${FONT}`;
            ctx.fillText(item.label, lx, 8);
            lx -= ctx.measureText(item.label).width + 6;
            ctx.fillStyle = item.color;
            ctx.fillRect(lx - 10, 7, 10, 2.5);
            lx -= 22;
        }
        // data, thinned out to about one point per pixel
        const n = series.t.length;
        const stride = Math.max(1, Math.floor(n / Math.max(1, plotWidth)));
        ctx.save();
        ctx.beginPath();
        ctx.rect(LEFT, 0, plotWidth, height);
        ctx.clip();
        ctx.lineWidth = 1.6;
        ctx.lineJoin = 'round';
        for (const item of LINES) {
            plot(ctx, series.t, series[item.key], stride, x, yPower, item.color);
        }
        plot(ctx, series.t, series.speed, stride, x, ySpeed, SPEED_COLOR);
        ctx.restore();
        // playhead
        if (cursor !== null && cursor >= t0 && cursor <= t1) {
            ctx.strokeStyle = 'rgba(248, 250, 252, 0.85)';
            ctx.lineWidth = 1.5;
            line(ctx, x(cursor), powerTop - 4, x(cursor), height - 12);
        }
        // hover readout
        if (this.hoverX !== null && this.hoverX >= LEFT && n > 0) {
            const t = this.timeAt(this.hoverX, range);
            const i = nearest(series.t, t);
            ctx.strokeStyle = 'rgba(148, 163, 184, 0.5)';
            ctx.setLineDash([3, 3]);
            line(ctx, x(series.t[i]), powerTop - 4, x(series.t[i]), height - 12);
            ctx.setLineDash([]);
            const text = `${series.t[i].toFixed(2)} s   fwd ${series.fwd[i].toFixed(2)}   strafe ${series.left[i].toFixed(2)}   ` +
                `turn ${series.turn[i].toFixed(2)}   ${series.speed[i].toFixed(1)} in/s`;
            ctx.font = `500 11px ${FONT}`;
            const w = ctx.measureText(text).width + 16;
            const bx = Math.min(width - RIGHT - w, Math.max(LEFT, x(series.t[i]) + 8));
            ctx.fillStyle = 'rgba(13, 17, 23, 0.95)';
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.14)';
            ctx.beginPath();
            ctx.roundRect(bx, speedTop - 26, w, 20, 6);
            ctx.fill();
            ctx.stroke();
            ctx.fillStyle = '#e6edf3';
            ctx.textAlign = 'left';
            ctx.fillText(text, bx + 8, speedTop - 16);
        }
    }
}
function plot(ctx, t, values, stride, x, y, color) {
    if (t.length < 2)
        return;
    ctx.strokeStyle = color;
    ctx.beginPath();
    for (let i = 0; i < t.length; i += stride) {
        if (i === 0)
            ctx.moveTo(x(t[i]), y(values[i]));
        else
            ctx.lineTo(x(t[i]), y(values[i]));
    }
    ctx.lineTo(x(t[t.length - 1]), y(values[values.length - 1]));
    ctx.stroke();
}
function line(ctx, x1, y1, x2, y2) {
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
}
/** Index of the time closest to t (times are sorted). */
function nearest(times, t) {
    let lo = 0;
    let hi = times.length - 1;
    while (lo < hi) {
        const mid = (lo + hi) >> 1;
        if (times[mid] < t)
            lo = mid + 1;
        else
            hi = mid;
    }
    return lo > 0 && Math.abs(times[lo - 1] - t) < Math.abs(times[lo] - t) ? lo - 1 : lo;
}
//# sourceMappingURL=chart.js.map