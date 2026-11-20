# Path Planner Academy

A purple, robot-themed learning game that teaches Pure Pursuit with the exact same Java
simulation backend as FieldViewer. It lives outside `FieldViewer/web` so it is easy to find.

## Start

From this folder:

```
npm start
```

The first launch requires the FieldViewer dependencies to be installed. If needed, run
`npm install` once inside `../FieldViewer/web`.

## Where things are

- `src/academy.ts` — missions, dragging, simulation playback, and confetti
- `static/index.html` — Academy page
- `static/academy.css` — purple design and animations

The Academy remains separate from the normal FieldViewer interface, but shares its local
Java backend and real Pure Pursuit calculations.
