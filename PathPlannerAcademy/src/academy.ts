type Vec = [number, number];
type Frame = (number | null)[];
type FieldInfo = { half: number; center: [number, number, number, number]; robot: [number, number] };
type SimResult = {
  dt: number;
  summary: { duration: number; finished: boolean; centerContact: number };
  frames: Frame[];
};
type RoutesResponse = { field: FieldInfo };
type Sketch = {
  name: string;
  points: [number, number, null, null][];
  reversed: boolean;
  moveSpeed: number;
  followDistance: number;
  startHeading: number;
  endHeading: number | null;
  fixedHeading: number | null;
};

const F = {
  T: 0, X: 1, Y: 2, H: 3, FX: 4, FY: 5, FR: 6,
  PATH: 15, STATUS: 17, COLLIDE: 18,
} as const;

async function fetchRoutes(): Promise<RoutesResponse> {
  const response = await fetch('/api/routes');
  if (!response.ok) throw new Error(`could not load routes (${response.status})`);
  return response.json() as Promise<RoutesResponse>;
}

async function simulateSketch(sketch: Sketch, holonomic: boolean): Promise<SimResult> {
  const response = await fetch('/api/simulate', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sketch, holonomic, follower: {}, drive: {}, robot: null }),
  });
  const result = await response.json() as SimResult & { error?: string };
  if (!response.ok || result.error) throw new Error(result.error ?? `simulation failed (${response.status})`);
  return result;
}

type LessonId = 'first' | 'waypoint' | 'lookahead' | 'hive' | 'localization' | 'drivetrain' | 'heading' | 'freeplay';
type Lesson = { id: LessonId; title: string; short: string; description: string; word: string; meaning: string; hint: string; coach: string; pass: string; goals: [string, string] };
const FIRST_LESSON: Lesson = { id: 'first', title: 'Meet FIRST and FTC', short: 'What is FIRST?', description: 'FIRST is a global nonprofit robotics community where young people work with adult mentors, solve a new game challenge, and learn by designing, building, programming, testing, documenting, and competing. Its programs grow with students: FIRST LEGO League, FIRST Tech Challenge, and FIRST Robotics Competition. Team 25 competes in FTC.', word: 'FIRST', meaning: 'FIRST stands for For Inspiration and Recognition of Science and Technology. FTC stands for FIRST Tech Challenge.', hint: 'Answer all three questions below', coach: 'You do not need robot experience to belong here. A FIRST team learns by doing: one student might design a bracket, another might wire a sensor, and another might test the code. When something fails, the team shares evidence, improves the idea, and tries again. That respectful teamwork is part of gracious professionalism.', pass: 'Answer all three multiple-choice FIRST questions correctly.', goals: ['Recognize FIRST as a hands-on robotics community with several programs.', 'Know what FTC means and how teams learn through a season challenge.'] };
type QuizQuestion = { prompt: string; answers: string[]; correct: number; explanation: string };
const FIRST_QUIZ: QuizQuestion[] = [
  { prompt: 'Which sentence best describes FIRST?', answers: ['A motor company', 'A robotics community where students solve challenges together', 'A remote control for robots'], correct: 1, explanation: 'FIRST is a community where students and mentors learn together through robotics challenges.' },
  { prompt: 'What does FTC mean?', answers: ['FIRST Tech Challenge', 'Field Tracking Controller', 'Fast Team Competition'], correct: 0, explanation: 'FTC means FIRST Tech Challenge—the program in which Team 25 competes.' },
  { prompt: 'What do FIRST teams do when an idea does not work yet?', answers: ['Hide the mistake', 'Stop building', 'Test, learn, and improve the design together'], correct: 2, explanation: 'Engineering is iterative: teams test, learn from evidence, and improve together.' },
];
const BASE_LESSONS: Lesson[] = [
  { id: 'waypoint', title: 'Give the robot a destination', short: 'Pick a waypoint', description: 'Tap somewhere on the field. That dot becomes a place the robot should travel through. Then hold the space key to watch it go.', word: 'Waypoint', meaning: 'A waypoint is one dot on the route. A path can use several waypoints to make a curve.', hint: 'Tap anywhere to move the goal', coach: 'The purple line is Buzzy’s plan, and the gold dot is the destination. For example, moving the gold dot from the top right to the bottom right creates a completely different trip. Hold Space to drive; releasing it pauses immediately without erasing the route.', pass: 'Move the gold destination, then hold Space or Drive until Buzzy reaches it.', goals: ['Recognize a waypoint as a place on a route.', 'Connect a changed endpoint to a changed robot path.'] },
  { id: 'lookahead', title: 'Let the robot look ahead', short: 'Tune lookahead', description: 'This route already travels safely around the Hive. Change the lookahead distance, then hold Space to see how the robot follows the same path differently.', word: 'Lookahead', meaning: 'A circle around the robot finds a target farther along the path. A bigger circle usually makes smoother, wider turns.', hint: 'Adjust Lookahead below, then hold Space', coach: 'Think of lookahead like looking farther down a hallway while walking. A small value makes Buzzy react closely to every bend; a large value aims farther ahead and usually creates a wider, smoother turn. It changes how Buzzy follows the purple route, but it never moves the route or invents obstacle avoidance.', pass: 'Change the Lookahead slider, then drive all the way to the gold destination.', goals: ['See how the lookahead circle chooses a follow point.', 'Notice that tuning changes motion, not the planned route.'] },
  { id: 'hive', title: 'Reach the corner without touching the Hive', short: 'Pass the Hive test', description: 'Click on the field to add a waypoint that sends Buzzy around the Hive. Buzzy must finish at the locked gold goal in the top-right corner—the goal itself cannot be moved.', word: 'Collision', meaning: 'A collision happens when the robot body overlaps an obstacle. The path planner must give the whole robot enough room.', hint: 'Click the field to add a detour waypoint', coach: 'A line can miss the Hive while Buzzy’s square body still clips a corner. For example, place a waypoint far to the left of the Hive and another above it so all four sides have clearance. This mission only passes at the original gold goal with zero Hive contact; Pure Pursuit will not repair an unsafe route for you.', pass: 'Click the field to add one or more waypoints, then reach the locked top-right goal without touching the Hive.', goals: ['Plan clearance for the whole robot, not just its center.', 'Understand that Pure Pursuit follows the route you provide.'] },
  { id: 'localization', title: 'Tell the robot where it starts', short: 'Use localization', description: 'Choose a starting corner and a destination. Pinpoint measures how the robot moves, while the starting pose anchors that motion on this field.', word: 'Localization', meaning: 'Localization is the robot’s best estimate of its X position, Y position, and heading angle.', hint: 'Choose a start and target below', coach: 'Pinpoint measures changes in position, but the code must first tell it where that movement began. Imagine placing Buzzy in the blue audience corner while the code believes it started across the field: every later measurement would be shifted to the wrong place. A correct starting X, Y, and heading anchors the whole route.', pass: 'Choose a start and Hive side, then drive from that field pose to the selected goal.', goals: ['Identify pose as X, Y, and heading.', 'Explain why Pinpoint still needs a correct starting pose.'] },
  { id: 'drivetrain', title: 'Compare mecanum and tank drive', short: 'Compare drivetrains', description: 'Run the same curving route with each drivetrain. Mecanum can combine forward, sideways, and turning motion; tank drive moves forward and turns, but cannot strafe.', word: 'Drivetrain', meaning: 'The drivetrain is the wheels, motors, and code that move the robot. Different wheel layouts can move in different ways.', hint: 'Choose a drivetrain, then hold Space', coach: 'Mecanum is like sliding a rolling chair sideways: its angled wheel rollers create forward and sideways motion together. Tank drive behaves more like a car—it must point into a turn before moving that direction. Try the same purple route with each option and watch whether the cyan front marker stays fixed or turns through the bend.', pass: 'Choose Mecanum or Tank, then drive the prepared outside-Hive route to its destination.', goals: ['Describe how mecanum wheels allow sideways motion.', 'Explain why a tank drive must steer instead of strafe.'] },
  { id: 'heading', title: 'Choose which way the robot faces', short: 'Change heading', description: 'Pick a field direction for the front of the robot, then hold Space. A mecanum robot can travel along the route while continuing to face that chosen direction.', word: 'Heading', meaning: 'Heading is the direction the front of the robot points. Position says where the robot is; heading says which way it faces.', hint: 'Choose a heading below, then hold Space', coach: 'The cyan triangle marks Buzzy’s front. Position and heading can be different: Buzzy might travel left while keeping its camera or shooter pointed up toward a target. Choose 90° to face up, then compare it with 0° to face right while following the same safe route.', pass: 'Choose a facing direction, then drive the prepared below-Hive route to the destination.', goals: ['Separate the robot’s position from its heading.', 'Use the cyan front marker to read the chosen direction.'] },
  { id: 'freeplay', title: 'Build your own robot challenge!', short: 'Free-play lab', description: 'Create a route, choose a drivetrain, and test a situation. This is the same Pure Pursuit simulation used by FieldViewer.', word: 'Scenario', meaning: 'A scenario is a made-up test situation: a route, a starting pose, obstacles, and settings to try safely.', hint: 'Tap to add path dots · Hold Space to test', coach: 'Combine everything you learned. For example: add three waypoints around the Hive, select Mecanum, lower the speed, and add another robot situation. If the run is awkward, change only one setting at a time so you can tell which change helped—exactly like testing the real robot.', pass: 'Add at least one destination, adjust any setting you want, and drive to the end.', goals: ['Combine waypoints, drivetrain choice, speed, and obstacles.', 'Use simulation to test an idea before trying it on a robot.'] },
];

const $ = <T extends HTMLElement = HTMLElement>(id: string): T => document.getElementById(id) as T;
const canvas = $<HTMLCanvasElement>('academy-field');
const ctx = canvas.getContext('2d')!;
let field: FieldInfo = { half: 72, center: [-25.5, -18, 25.5, 18], robot: [17, 17] };
let lesson = 0, elapsed = 0, speed = .62, lookahead = 12, facingHeading = 0, requestVersion = 0, simulationTimer = 0, toastTimer = 0;
let points: Vec[] = [[-55, -47], [38, 31]];
let result: SimResult | null = null, frame: Frame | null = null;
let held = false, holonomic = true, surpriseRobot = false, reachedSurprise = false, lastTick = performance.now();
let startChoice = 'blue-audience', targetChoice = 'blue-hive';
let draggingWaypoint = false, dragMoved = false, dragPointer = -1, dragIndex = -1;
let missionActionDone = false, firstQuizIndex = 0;
let LESSONS: Lesson[] = BASE_LESSONS;
let progressKey = 'academy-progress-v5-ftc';
const HIVE_TEST_GOAL: Vec = [55, 40];
let completed = new Set<number>();
const n = (f: Frame, i: number): number => (f[i] as number | null) ?? 0;
const clamp = (v: number, lo: number, hi: number): number => Math.max(lo, Math.min(hi, v));
const lessonId = (): LessonId => LESSONS[lesson].id;

function applyLocalizationChoices(): void {
  const starts: Record<string, Vec> = { 'blue-audience': [-58,-50], 'blue-far': [58,-50] };
  const targets: Record<string, Vec> = { 'blue-hive': [-34,-22], 'blue-far-hive': [34,-22] };
  points = [starts[startChoice], targets[targetChoice]];
}
function defaults(index: number): void {
  elapsed = 0; held = false; surpriseRobot = false; reachedSurprise = false; missionActionDone = false;
  facingHeading = 0; firstQuizIndex = 0;
  const id=LESSONS[index].id;
  if (id === 'first') { points = []; holonomic = true; }
  if (id === 'waypoint') { points = [[-55,-47],[38,31]]; lookahead = 12; speed = .62; holonomic = true; }
  if (id === 'lookahead') { points = [[-57,-42],[-58,42],[0,60],[55,43]]; lookahead = 10; speed = .65; holonomic = true; }
  if (id === 'hive') { points = [[-55,-43],HIVE_TEST_GOAL]; lookahead = 11; speed = .58; holonomic = true; }
  if (id === 'localization') applyLocalizationChoices();
  if (id === 'drivetrain') { points = [[-55,-45],[-15,-58],[25,-58],[55,-45]]; lookahead = 12; speed = .62; holonomic = true; }
  if (id === 'heading') { points = [[-55,-45],[-60,0],[-55,45]]; lookahead = 12; speed = .62; holonomic = true; }
  if (id === 'freeplay') { points = [[-55,-48]]; lookahead = 12; speed = .62; holonomic = true; }
  $('hold-drive').classList.remove('held');
}
function lessonList(): void {
  const unlocked = firstIncompleteLesson();
  $('lesson-list').innerHTML = LESSONS.slice(0, unlocked + 1).map((item, i) => `<button class="lesson-button ${i===lesson?'active':''} ${completed.has(i)?'done':''}" data-lesson="${i}"><span class="number">${completed.has(i)?'✓':i+1}</span><span><b>${item.short}</b><small>${item.id==='first'?'Welcome':item.id==='freeplay'?'Sandbox':`Mission ${i+1}`}</small></span><span class="check">${completed.has(i)?'★':''}</span></button>`).join('');
  document.querySelectorAll<HTMLButtonElement>('[data-lesson]').forEach(b => b.addEventListener('click', () => selectLesson(Number(b.dataset.lesson))));
  $('star-count').textContent = String(completed.size);
  $('star-total').textContent = String(LESSONS.length);
}
function firstIncompleteLesson(): number {
  for (let i = 0; i < LESSONS.length; i++) if (!completed.has(i)) return i;
  return LESSONS.length - 1;
}
function selectLesson(index: number): void {
  lesson = clamp(index, 0, firstIncompleteLesson()); defaults(lesson); renderLesson();
  if (lessonId() !== 'first') queueSimulation();
}
function renderLesson(): void {
  const item = LESSONS[lesson];
  $('lesson-kicker').textContent = item.id==='first' ? 'WELCOME TO FIRST' : item.id==='freeplay' ? 'FINAL FREE-PLAY LAB' : `MISSION ${lesson+1} OF ${LESSONS.length}`;
  $('lesson-title').textContent = item.title; $('lesson-description').textContent = item.description;
  $('word-name').textContent = item.word; $('word-meaning').textContent = item.meaning;
  $('pass-text').textContent = item.pass;
  $('goal-list').innerHTML = item.goals.map(goal => `<li>${goal}</li>`).join('');
  $('touch-hint').textContent = item.hint; $('coach-message').textContent = item.coach;
  $('complete-lesson').innerHTML = item.id==='freeplay' ? 'Finish lesson <b>★</b>' : 'I understand! <b>Next</b>';
  $('sim-card').classList.toggle('intro-lesson',item.id==='first');
  $('sim-title').textContent=item.id==='first'?'FIRST Starter Quiz':'Learning Field';
  if (item.id==='first') $('sim-status').textContent='Choose one answer to continue';
  let text = [['Dots become a route','The route joins your waypoints in order. The robot begins at the first dot.'],['Find a follow point','The follower intersects its lookahead circle with the route and chooses a point ahead.'],['Correct every loop','The robot repeatedly steers toward that point. New measurements update the next correction.']];
  if (item.id==='first') text = [['A challenge starts it','FIRST teams receive a new game challenge to solve.'],['Students build together','Teams design, program, test, document, and drive their robots.'],['Skills grow through trying','Ideas improve through teamwork, mistakes, and another attempt.']];
  if (item.id==='drivetrain') text = [['Use the same route','A fair comparison keeps the waypoints, speed, and lookahead the same.'],['Mecanum can strafe','Angled rollers let it move forward and sideways at the same time.'],['Tank must steer','Left and right wheels cannot slide sideways, so the robot turns its front to change direction.']];
  if (item.id==='heading') text = [['Position is the place','The route still decides where the robot travels on the field.'],['Heading is the direction','Your heading choice decides where the cyan front triangle points.'],['Mecanum separates both','It can translate along the route while holding a different field-facing direction.']];
  ['plan','look','move'].forEach((name,i) => { $(`${name}-text`).textContent=text[i][0]; $(`${name}-detail`).textContent=text[i][1]; });
  renderControls(); lessonList(); updateMissionButton();
}
function chip(label: string, action: string, active = false, danger = false): string { return `<button class="chip-button ${active?'active':''} ${danger?'danger':''}" data-action="${action}">${label}</button>`; }
function renderControls(): void {
  const c = $('mission-controls');
  const id=lessonId();
  if (id === 'first') {
    const question=FIRST_QUIZ[firstQuizIndex];
    c.innerHTML = `<div class="first-quiz"><div class="quiz-progress">QUESTION ${firstQuizIndex+1} OF ${FIRST_QUIZ.length}</div><p>${question.prompt}</p><div class="quiz-answers">${question.answers.map((answer,i)=>chip(answer,`quiz-answer-${i}`)).join('')}</div><div id="quiz-feedback" role="status">Choose one answer. You can try again if needed.</div></div>`;
  }
  if (id === 'waypoint') c.innerHTML = chip('Try a short path','short') + chip('Try a long path','long');
  if (id === 'lookahead') c.innerHTML = `${chip('Show a safe route','outside-lookahead')}${chip('Undo dot','undo')}<label class="slider-control">Lookahead <input id="lookahead-slider" type="range" min="6" max="28" value="${lookahead}"><output id="lookahead-value">${lookahead} in</output></label>`;
  if (id === 'hive') c.innerHTML = chip('Safe detour','safe') + chip('Unsafe straight line','unsafe',points.length===2) + chip('Undo dot','undo');
  if (id === 'localization') c.innerHTML = chip('Blue audience start','blue-audience',startChoice==='blue-audience') + chip('Blue far start','blue-far',startChoice==='blue-far') + chip('Blue Hive side','blue-hive',targetChoice==='blue-hive') + chip('Far Hive side','blue-far-hive',targetChoice==='blue-far-hive');
  if (id === 'drivetrain') c.innerHTML = chip('Try mecanum','mecanum',holonomic) + chip('Try tank','tank',!holonomic);
  if (id === 'heading') c.innerHTML = chip('Face right · 0°','face-0',facingHeading===0) + chip('Face up · 90°','face-90',facingHeading===Math.PI/2) + chip('Face left · 180°','face-180',facingHeading===Math.PI) + chip('Face down · −90°','face--90',facingHeading===-Math.PI/2);
  if (id === 'freeplay') c.innerHTML = chip('Mecanum','mecanum',holonomic) + chip('Tank','tank',!holonomic) + chip('Add robot situation','surprise',surpriseRobot) + chip('Undo dot','undo') + chip('Clear','clear',false,true) + `<label class="slider-control">Speed <input id="speed-slider" type="range" min="25" max="90" step="5" value="${Math.round(speed*100)}"><output id="speed-value">${Math.round(speed*100)}%</output></label>`;
  c.querySelectorAll<HTMLButtonElement>('[data-action]').forEach(b => b.addEventListener('click', () => action(b.dataset.action!)));
  $('lookahead-slider')?.addEventListener('input', e => { missionActionDone=true; lookahead=Number((e.target as HTMLInputElement).value); $('lookahead-value').textContent=`${lookahead} in`; queueSimulation(); });
  $('speed-slider')?.addEventListener('input', e => { speed=Number((e.target as HTMLInputElement).value)/100; $('speed-value').textContent=`${Math.round(speed*100)}%`; queueSimulation(); });
}
function action(a: string): void {
  if (a.startsWith('quiz-answer-')) {
    const answer=Number(a.slice('quiz-answer-'.length)),question=FIRST_QUIZ[firstQuizIndex];
    if (answer!==question.correct) {
      $('quiz-feedback').textContent='Not quite. Re-read the introduction and try another answer.';
      $('quiz-feedback').classList.add('wrong'); return;
    }
    if (firstQuizIndex<FIRST_QUIZ.length-1) {
      showToast(`Correct! ${question.explanation}`); firstQuizIndex++; renderControls(); return;
    }
    $('quiz-feedback').textContent=`Correct! ${question.explanation}`;
    $('quiz-feedback').classList.remove('wrong'); missionActionDone=true; completeCurrentLesson(); return;
  }
  missionActionDone = true;
  if (a==='short') points=[[-48,-38],[5,5]]; if (a==='long') points=[[-58,-50],[54,46]];
  if (a==='outside-lookahead') points=[[-57,-42],[-58,42],[0,60],[55,43]];
  if (a==='safe') points=[[-55,-43],[-58,42],[0,60],HIVE_TEST_GOAL]; if (a==='unsafe') points=[[-55,-43],HIVE_TEST_GOAL];
  if (a==='undo' && points.length>1) points.pop(); if (a==='clear') points=[points[0]??[-55,-48]];
  if (a==='mecanum') holonomic=true; if (a==='tank') holonomic=false; if (a==='surprise') surpriseRobot=!surpriseRobot;
  if (a==='face-0') facingHeading=0; if (a==='face-90') facingHeading=Math.PI/2;
  if (a==='face-180') facingHeading=Math.PI; if (a==='face--90') facingHeading=-Math.PI/2;
  if (a==='blue-audience'||a==='blue-far') { startChoice=a; applyLocalizationChoices(); }
  if (a==='blue-hive'||a==='blue-far-hive') { targetChoice=a; applyLocalizationChoices(); }
  elapsed=0; reachedSurprise=false; renderControls(); queueSimulation();
}
function queueSimulation(): void {
  if (lessonId()==='first') return;
  clearTimeout(simulationTimer); requestVersion++; endHold(); result=null; frame=null; elapsed=0;
  $('sim-status').textContent=points.length<2?'Add one more path dot':'Planning with real Pure Pursuit math…';
  simulationTimer=window.setTimeout(runSimulation,110); draw();
}
async function runSimulation(): Promise<void> {
  if (points.length<2) { $('sim-status').textContent='Add one more path dot'; return; }
  const version=requestVersion;
  try {
    const sim=await simulateSketch({name:`Academy mission ${lesson+1}`,points:points.map(([x,y])=>[x,y,null,null]),reversed:false,moveSpeed:speed,followDistance:lookahead,startHeading:lessonId()==='heading'?facingHeading:0,endHeading:null,fixedHeading:holonomic?facingHeading:null},holonomic);
    if (version!==requestVersion) return; result=sim; frame=sim.frames[0]??null; elapsed=0;
    $('sim-status').textContent=sim.summary.finished?'Path ready—hold Drive!':'Path ready (the follower may time out)'; draw();
  } catch (error) { if (version!==requestVersion) return; $('sim-status').textContent=`Could not plan: ${(error as Error).message}`; showToast('The Academy backend is not ready yet. Try Reset in a moment.'); }
}
function screenToField(e: PointerEvent): Vec { const r=canvas.getBoundingClientRect(); return [clamp((e.clientX-r.left)/r.width*field.half*2-field.half,-69,69),clamp(field.half-(e.clientY-r.top)/r.height*field.half*2,-69,69)]; }
function hitWaypoint(e: PointerEvent): number {
  const id=lessonId();
  if (points.length < 2 || !['waypoint','lookahead','hive','freeplay'].includes(id)) return -1;
  const r = canvas.getBoundingClientRect();
  const first = id==='waypoint' ? points.length-1 : 1;
  let closest = -1, closestDistance = 43;
  const end = id==='hive' ? points.length-1 : points.length;
  for (let i=first;i<end;i++) {
    const point=xy(points[i]);
    const px=point[0]/canvas.width*r.width, py=point[1]/canvas.height*r.height;
    const distance=Math.hypot(e.clientX-r.left-px,e.clientY-r.top-py);
    if (distance<closestDistance) { closest=i; closestDistance=distance; }
  }
  return closest;
}
function fieldPointerDown(e: PointerEvent): void {
  const hit=hitWaypoint(e);
  if (hit>=0) {
    e.preventDefault(); draggingWaypoint = true; dragMoved = false; dragPointer = e.pointerId; dragIndex=hit;
    try { canvas.setPointerCapture(e.pointerId); } catch { /* Dragging still works without capture. */ }
    endHold(); result = null; frame = null;
    $('sim-status').textContent = 'Dragging path dot—release to calculate the new route';
    canvas.classList.add('dragging');
  }
}
function fieldPointerMove(e: PointerEvent): void {
  if (!draggingWaypoint || e.pointerId !== dragPointer || dragIndex<0) return;
  points[dragIndex] = screenToField(e); dragMoved = true; draw();
}
function fieldPointerUp(e: PointerEvent): void {
  if (draggingWaypoint && e.pointerId === dragPointer && dragIndex>=0) {
    points[dragIndex] = screenToField(e); draggingWaypoint = false; dragPointer = -1; dragIndex=-1;
    canvas.classList.remove('dragging');
    if (canvas.hasPointerCapture(e.pointerId)) canvas.releasePointerCapture(e.pointerId);
    dragMoved = false; missionActionDone = true; queueSimulation(); return;
  }
  if (dragMoved) { dragMoved = false; return; }
  const p=screenToField(e);
  const id=lessonId();
  if (id==='waypoint') points=[points[0],p];
  else if (id==='lookahead') points.splice(points.length-1,0,p);
  else if (id==='hive') points.splice(points.length-1,0,p);
  else if (id==='freeplay') points.push(p);
  else return;
  missionActionDone = true;
  queueSimulation();
}
function startDriving(): void {
  if (!result?.frames.length) { showToast(points.length<2?'Add another path dot first!':'The path is still being planned.'); return; }
  if (elapsed>=result.summary.duration) elapsed=0;
  held=true; reachedSurprise=false; lastTick=performance.now();
  $('hold-drive').classList.add('held'); $('sim-status').textContent='Driving—release Space or the button to stop';
}
function beginHold(e: PointerEvent): void {
  e.preventDefault(); startDriving();
  try { (e.currentTarget as HTMLElement).setPointerCapture(e.pointerId); }
  catch { /* Driving still works when Electron cannot capture this pointer. */ }
}
function beginMouseHold(e: MouseEvent): void { if (!held && e.button===0) { e.preventDefault(); startDriving(); } }
function endHold(): void { if (!held) return; held=false; $('hold-drive').classList.remove('held'); $('sim-status').textContent='Stopped—hold Space to continue'; }
function isTextEntry(target: EventTarget | null): boolean {
  return target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement ||
    target instanceof HTMLSelectElement || (target instanceof HTMLElement && target.isContentEditable);
}
function keyboardDown(e: KeyboardEvent): void {
  if (e.code!=='Space' || isTextEntry(e.target)) return;
  e.preventDefault();
  if (!e.repeat && !held) startDriving();
}
function keyboardUp(e: KeyboardEvent): void {
  if (e.code!=='Space' || isTextEntry(e.target)) return;
  e.preventDefault(); endHold();
}
function resetRun(): void { endHold(); elapsed=0; frame=result?.frames[0]??null; reachedSurprise=false; $('sim-status').textContent=result?'Back at the start':'Planning…'; draw(); }
function currentFrame(time:number):Frame|null { if (!result?.frames.length) return null; let lo=0,hi=result.frames.length-1; while(lo<hi){const mid=Math.ceil((lo+hi)/2); if(n(result.frames[mid],F.T)<=time)lo=mid;else hi=mid-1;} return result.frames[lo]; }
function tick(now:number):void {
  if(held&&result){elapsed+=Math.min((now-lastTick)/1000,.08);frame=currentFrame(elapsed);
    if(surpriseRobot&&lessonId()==='freeplay'&&frame&&Math.hypot(n(frame,F.X)-8,n(frame,F.Y)-4)<18&&!reachedSurprise){reachedSurprise=true;endHold();showToast('The teaching demo paused at another robot. In a real match, use your team’s planned driver-control and safety procedure.');}
    if(elapsed>=result.summary.duration){held=false;$('hold-drive').classList.remove('held');$('sim-status').textContent=result.summary.finished?'Destination reached!':'Simulation ended before settling';maybeCompleteMission();}}
  lastTick=now;draw();requestAnimationFrame(tick);
}
function resize():void { const r=canvas.getBoundingClientRect(),d=Math.min(devicePixelRatio||1,2),w=Math.max(1,Math.round(r.width*d)),h=Math.max(1,Math.round(r.height*d)); if(canvas.width!==w||canvas.height!==h){canvas.width=w;canvas.height=h;} }
function xy(p:Vec):Vec { const s=canvas.width/(field.half*2); return [(p[0]+field.half)*s,(field.half-p[1])*s]; }
function drawField():void {
  const size=canvas.width,s=size/(field.half*2),g=ctx.createLinearGradient(0,0,size,size);g.addColorStop(0,'#17102f');g.addColorStop(1,'#29144b');ctx.fillStyle=g;ctx.fillRect(0,0,size,canvas.height);
  ctx.lineWidth=Math.max(1,s*.2);ctx.strokeStyle='rgba(194,177,255,.18)';for(let v=-72;v<=72;v+=24){const [x]=xy([v,0]),[,y]=xy([0,v]);ctx.beginPath();ctx.moveTo(x,0);ctx.lineTo(x,size);ctx.stroke();ctx.beginPath();ctx.moveTo(0,y);ctx.lineTo(size,y);ctx.stroke();}
  ctx.strokeStyle='rgba(255,255,255,.35)';ctx.lineWidth=Math.max(2,s*.5);ctx.strokeRect(1,1,size-2,size-2);
  const [a,b,c,d]=field.center,[left,top]=xy([a,d]),[right,bottom]=xy([c,b]);ctx.fillStyle='rgba(255,196,75,.16)';ctx.strokeStyle='#ffca55';ctx.setLineDash([s*2,s*1.4]);ctx.lineWidth=Math.max(2,s*.7);ctx.fillRect(left,top,right-left,bottom-top);ctx.strokeRect(left,top,right-left,bottom-top);ctx.setLineDash([]);ctx.fillStyle='#ffdd83';ctx.font=`${Math.max(10,s*3.3)}px system-ui`;ctx.textAlign='center';ctx.fillText('HIVE',(left+right)/2,(top+bottom)/2+4);
}
function drawPath():void {
  if(!points.length)return;ctx.lineCap='round';ctx.lineJoin='round';ctx.beginPath();points.forEach((p,i)=>{const q=xy(p);i?ctx.lineTo(q[0],q[1]):ctx.moveTo(q[0],q[1]);});ctx.strokeStyle='#a98cf4';ctx.lineWidth=Math.max(3,canvas.width/110);ctx.stroke();
  if(frame&&result){const index=result.frames.indexOf(frame);ctx.beginPath();result.frames.slice(0,Math.max(index,1)).forEach((f,i)=>{const q=xy([n(f,F.X),n(f,F.Y)]);i?ctx.lineTo(q[0],q[1]):ctx.moveTo(q[0],q[1]);});ctx.strokeStyle='#3ed5dc';ctx.lineWidth=Math.max(2,canvas.width/180);ctx.stroke();}
  const id=lessonId();points.forEach((p,i)=>{const q=xy(p),endpoint=i===points.length-1,editable=i>0&&['waypoint','lookahead','hive','freeplay'].includes(id),r=endpoint?Math.max(10,canvas.width/58):editable?Math.max(8,canvas.width/70):Math.max(5,canvas.width/80);if(endpoint||editable){ctx.beginPath();ctx.arc(q[0],q[1],r+7,0,Math.PI*2);ctx.fillStyle=endpoint?'rgba(255,202,85,.2)':'rgba(245,138,198,.2)';ctx.fill();}ctx.beginPath();ctx.arc(q[0],q[1],r,0,Math.PI*2);ctx.fillStyle=i===0?'#5ed49a':endpoint?'#ffca55':'#f58ac6';ctx.fill();ctx.strokeStyle='white';ctx.lineWidth=3;ctx.stroke();if(endpoint){ctx.fillStyle='#301b56';ctx.font=`900 ${Math.max(9,canvas.width/70)}px system-ui`;ctx.textAlign='center';ctx.textBaseline='middle';ctx.fillText(id==='hive'?'◆':'↕',q[0],q[1]);}});
}
function drawLookahead(f:Frame):void {const fr=f[F.FR],fx=f[F.FX],fy=f[F.FY];if(fr===null||fx===null||fy===null)return;const q=xy([n(f,F.X),n(f,F.Y)]),s=canvas.width/(field.half*2);ctx.beginPath();ctx.arc(q[0],q[1],(fr as number)*s,0,Math.PI*2);ctx.strokeStyle='rgba(62,213,220,.85)';ctx.lineWidth=2;ctx.setLineDash([6,5]);ctx.stroke();ctx.setLineDash([]);const t=xy([fx as number,fy as number]);ctx.beginPath();ctx.arc(t[0],t[1],7,0,Math.PI*2);ctx.fillStyle='#3ed5dc';ctx.fill();ctx.strokeStyle='white';ctx.lineWidth=2;ctx.stroke();}
function drawRobot(f:Frame):void {const q=xy([n(f,F.X),n(f,F.Y)]),s=canvas.width/(field.half*2),l=field.robot[0]*s,w=field.robot[1]*s;ctx.save();ctx.translate(q[0],q[1]);ctx.rotate(-n(f,F.H));ctx.fillStyle=n(f,F.COLLIDE)===1?'#ef6680':'#7650e8';ctx.strokeStyle='white';ctx.lineWidth=Math.max(2,s*.6);ctx.beginPath();ctx.roundRect(-l/2,-w/2,l,w,5);ctx.fill();ctx.stroke();ctx.fillStyle='#3ed5dc';ctx.beginPath();ctx.moveTo(l/2+7,0);ctx.lineTo(l/2-5,-7);ctx.lineTo(l/2-5,7);ctx.closePath();ctx.fill();ctx.restore();}
function drawSurprise():void {if(!surpriseRobot||lessonId()!=='freeplay')return;const q=xy([8,4]),s=canvas.width/144;ctx.save();ctx.translate(q[0],q[1]);ctx.rotate(.25);ctx.fillStyle='#f58ac6';ctx.strokeStyle='white';ctx.lineWidth=2;ctx.fillRect(-8*s,-8*s,16*s,16*s);ctx.strokeRect(-8*s,-8*s,16*s,16*s);ctx.restore();}
function draw():void {resize();drawField();drawPath();drawSurprise();const f=frame??result?.frames[0];if(f){drawLookahead(f);drawRobot(f);$('coordinate-readout').textContent=`X ${n(f,F.X).toFixed(1)} · Y ${n(f,F.Y).toFixed(1)} · ${(n(f,F.H)*180/Math.PI).toFixed(0)}°`;}else if(points.length){const fake:Frame=[0,points[0][0],points[0][1],0,null,null,null,null,null,null,null,null,0,0,0,0,0,0,0,0,0];drawRobot(fake);}const id=lessonId(),pathHitsHive=(id==='lookahead'||id==='hive')&&Boolean(result&&result.summary.centerContact>0);if((f&&n(f,F.COLLIDE)===1)||pathHitsHive){$('canvas-callout').textContent='This route touches the Hive. Drag a pink dot farther outside.';$('canvas-callout').classList.add('show');}else $('canvas-callout').classList.remove('show');}
function showToast(message:string):void {const t=$('academy-toast');t.textContent=message;t.classList.add('show');clearTimeout(toastTimer);toastTimer=window.setTimeout(()=>t.classList.remove('show'),3600);}
function showCompletionDialog(): void {
  const dialog = $<HTMLDialogElement>('completion-dialog');
  if (!dialog.open) dialog.showModal();
}
function rainConfetti(): void {
  const shower = document.createElement('div'); shower.className = 'confetti-shower';
  const colors = ['#7650e8','#3ed5dc','#ffca55','#f58ac6','#5ed49a','#ffffff'];
  for (let i=0;i<90;i++) {
    const piece=document.createElement('i');
    piece.style.setProperty('--x',`${Math.random()*100}vw`);
    piece.style.setProperty('--delay',`${Math.random()*.55}s`);
    piece.style.setProperty('--fall',`${75+Math.random()*35}vh`);
    piece.style.setProperty('--turn',`${360+Math.random()*900}deg`);
    piece.style.background=colors[i%colors.length];
    shower.append(piece);
  }
  document.body.append(shower); window.setTimeout(()=>shower.remove(),2800);
}
function updateMissionButton(): void {
  const button = $<HTMLButtonElement>('complete-lesson');
  const done = completed.has(lesson);
  button.disabled = !done;
  button.innerHTML = done ? (lessonId()==='freeplay'?'All missions complete! <b>★</b>':'Next mission') : 'Finish this mission first';
}
function completeCurrentLesson(): void {
  if (completed.has(lesson)) return;
  completed.add(lesson); localStorage.setItem(progressKey,JSON.stringify([...completed]));
  rainConfetti(); lessonList(); updateMissionButton(); showToast(`Mission ${lesson+1} complete—star earned!`);
  if (completed.size === LESSONS.length) window.setTimeout(showCompletionDialog, 650);
}
function maybeCompleteMission(): void {
  if (completed.has(lesson) || !result?.summary.finished || !missionActionDone) return;
  const id=lessonId();
  if ((id==='lookahead' || id==='hive') && result.summary.centerContact>0) {
    showToast('The robot touched the Hive. Add a safer detour and try again!'); return;
  }
  if (id==='hive') {
    const end=points[points.length-1];
    if (!end || Math.hypot(end[0]-HIVE_TEST_GOAL[0],end[1]-HIVE_TEST_GOAL[1])>.1) {
      showToast('Finish at the locked gold goal in the top-right corner.'); return;
    }
  }
  completeCurrentLesson();
}
function completeLesson():void {
  if (!completed.has(lesson)) { showToast('Complete the activity and drive the robot to its goal first.'); return; }
  if (lesson<LESSONS.length-1) selectLesson(lesson+1); else showCompletionDialog();
}
function chooseLearnerMode(mode: 'ftc' | 'new'): void {
  LESSONS=mode==='new'?[FIRST_LESSON,...BASE_LESSONS]:BASE_LESSONS;
  progressKey=`academy-progress-v5-${mode}`;
  if (mode==='ftc' && !localStorage.getItem(progressKey) && localStorage.getItem('academy-progress-v4')) {
    localStorage.setItem(progressKey,localStorage.getItem('academy-progress-v4')!);
  }
  completed=new Set<number>(JSON.parse(localStorage.getItem(progressKey)??'[]') as number[]);
  $('completion-stars').textContent=Array(LESSONS.length).fill('★').join(' ');
  lesson=firstIncompleteLesson();
  defaults(lesson); renderLesson();
  if (lessonId()!=='first') queueSimulation();
  $<HTMLDialogElement>('onboarding-dialog').close();
  showToast(mode==='new'?'Begin with your FIRST welcome mission!':'FTC path selected—welcome back!');
}
async function init(): Promise<void> {
  canvas.addEventListener('pointerdown', fieldPointerDown);
  canvas.addEventListener('pointermove', fieldPointerMove);
  canvas.addEventListener('pointerup', fieldPointerUp);
  canvas.addEventListener('pointercancel', fieldPointerUp);
  const hold = $('hold-drive');
  hold.addEventListener('pointerdown', beginHold);
  hold.addEventListener('mousedown', beginMouseHold);
  hold.addEventListener('pointerup', endHold);
  hold.addEventListener('pointercancel', endHold);
  window.addEventListener('pointerup', endHold);
  window.addEventListener('mouseup', endHold);
  window.addEventListener('keydown', keyboardDown);
  window.addEventListener('keyup', keyboardUp);
  window.addEventListener('blur', endHold);
  $('reset-run').addEventListener('click', resetRun);
  $('complete-lesson').addEventListener('click', completeLesson);
  const about=$<HTMLDialogElement>('about-dialog');
  $('about-open').addEventListener('click',()=>about.showModal());
  $('about-close').addEventListener('click',()=>about.close());
  about.addEventListener('click',e=>{if(e.target===about)about.close();});
  const completion=$<HTMLDialogElement>('completion-dialog');
  $('completion-close').addEventListener('click',()=>completion.close());
  completion.addEventListener('click',e=>{if(e.target===completion)completion.close();});
  const onboarding=$<HTMLDialogElement>('onboarding-dialog');
  $('onboarding-ftc').addEventListener('click',()=>chooseLearnerMode('ftc'));
  $('onboarding-new').addEventListener('click',()=>chooseLearnerMode('new'));
  onboarding.addEventListener('cancel',e=>e.preventDefault());
  onboarding.showModal();
  window.addEventListener('resize', draw);
  requestAnimationFrame(tick);
  try { field = (await fetchRoutes()).field; }
  catch { showToast('Using the practice field while route information loads.'); }
}

void init();
