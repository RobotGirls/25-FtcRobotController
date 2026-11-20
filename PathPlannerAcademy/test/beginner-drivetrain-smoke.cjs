const { app, BrowserWindow } = require('electron');

const url = process.env.ACADEMY_TEST_URL || 'http://localhost:2526/academy/index.html';

async function waitFor(window, expression, message, attempts = 240) {
  for (let attempt = 0; attempt < attempts; attempt++) {
    if (await window.webContents.executeJavaScript(expression)) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  const status = await window.webContents.executeJavaScript(
    `document.getElementById('sim-status')?.textContent`,
  );
  throw new Error(`${message}: ${status}`);
}

app.whenReady().then(async () => {
  const window = new BrowserWindow({ show: false, webPreferences: { backgroundThrottling: false } });
  try {
    await window.loadURL(url);
    await window.webContents.executeJavaScript(
      `localStorage.setItem('academy-progress-v5-new',JSON.stringify([0,1,2,3,4])); location.reload()`,
    );
    await waitFor(window, `document.getElementById('onboarding-dialog')?.open === true`,
      'Onboarding did not appear');
    await window.webContents.executeJavaScript(`document.getElementById('onboarding-new').click()`);
    await waitFor(window, `document.getElementById('lesson-title').textContent === 'Compare mecanum and tank drive'`,
      'Beginner Task 6 did not open');
    await window.webContents.executeJavaScript(`document.querySelector('[data-action="mecanum"]').click()`);
    await waitFor(window, `document.getElementById('sim-status').textContent.includes('Path ready')`,
      'Mecanum route was not planned');
    await window.webContents.executeJavaScript(`document.getElementById('hold-drive').dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, cancelable: true, pointerId: 71, pointerType: 'mouse', button: 0 })
    )`);
    await waitFor(window, `document.getElementById('star-count').textContent === '6'`,
      'Beginner Task 6 mecanum run did not complete');
    console.log('Beginner Task 6 mecanum completion passed');
    app.exit(0);
  } catch (error) {
    console.error(error);
    app.exit(1);
  }
});
