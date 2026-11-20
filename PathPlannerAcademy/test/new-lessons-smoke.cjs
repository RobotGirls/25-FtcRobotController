const { app, BrowserWindow } = require('electron');

const url = process.env.ACADEMY_TEST_URL || 'http://localhost:2526/academy/index.html';

async function waitFor(window, expression, message, attempts = 180) {
  for (let attempt = 0; attempt < attempts; attempt++) {
    if (await window.webContents.executeJavaScript(expression)) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  const status = await window.webContents.executeJavaScript(
    `document.getElementById('sim-status')?.textContent`,
  );
  throw new Error(`${message}: ${status}`);
}

async function loadMission(window, completed, mission) {
  await window.webContents.executeJavaScript(
    `localStorage.setItem('academy-progress-v5-ftc', JSON.stringify(${JSON.stringify(completed)})); location.reload()`,
  );
  await waitFor(window, `Boolean(document.getElementById('onboarding-ftc'))`,
    'FTC onboarding choice did not appear');
  await window.webContents.executeJavaScript(`document.getElementById('onboarding-ftc').click()`);
  await waitFor(window, `Boolean(document.querySelector('[data-lesson="${mission}"]'))`,
    `Mission ${mission + 1} did not unlock`);
  await window.webContents.executeJavaScript(
    `document.querySelector('[data-lesson="${mission}"]').click()`,
  );
}

async function clickAction(window, action) {
  await window.webContents.executeJavaScript(
    `document.querySelector('[data-action="${action}"]').click()`,
  );
  await waitFor(window, `document.getElementById('sim-status')?.textContent?.includes('Path ready')`,
    `${action} route was not planned`);
}

async function driveToEnd(window) {
  await window.webContents.executeJavaScript(`document.getElementById('hold-drive').dispatchEvent(
    new PointerEvent('pointerdown', { bubbles: true, cancelable: true, pointerId: 61, pointerType: 'mouse', button: 0 })
  )`);
  await waitFor(window,
    `document.getElementById('sim-status')?.textContent === 'Destination reached!'`,
    'Robot did not reach the destination', 240);
}

app.whenReady().then(async () => {
  const window = new BrowserWindow({ show: false, webPreferences: { backgroundThrottling: false } });
  try {
    await window.loadURL(url);

    await loadMission(window, [0, 1], 2);
    await clickAction(window, 'unsafe');
    await driveToEnd(window);
    const unsafeStars = await window.webContents.executeJavaScript(
      `document.getElementById('star-count').textContent`,
    );
    if (unsafeStars !== '2') throw new Error(`Hive collision incorrectly passed (${unsafeStars} stars)`);
    await clickAction(window, 'safe');
    await driveToEnd(window);
    await waitFor(window, `document.getElementById('star-count').textContent === '3'`,
      'Safe fixed-goal Hive route did not pass');

    await loadMission(window, [0, 1, 2, 3], 4);
    await clickAction(window, 'mecanum');
    await driveToEnd(window);
    await waitFor(window, `document.getElementById('star-count').textContent === '5'`,
      'Mecanum/tank lesson did not pass');

    await loadMission(window, [0, 1, 2, 3, 4], 5);
    await clickAction(window, 'face-90');
    const heading = await window.webContents.executeJavaScript(
      `document.getElementById('coordinate-readout').textContent`,
    );
    if (!heading.includes('90°')) throw new Error(`Heading control did not face up: ${heading}`);
    await driveToEnd(window);
    await waitFor(window, `document.getElementById('star-count').textContent === '6'`,
      'Heading lesson did not pass');

    console.log('Hive collision gate, drivetrain lesson, and heading lesson passed');
    app.exit(0);
  } catch (error) {
    console.error(error);
    app.exit(1);
  }
});
