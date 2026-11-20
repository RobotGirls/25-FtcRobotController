const { app, BrowserWindow } = require('electron');

const url = process.env.ACADEMY_TEST_URL || 'http://localhost:2526/academy/index.html';

async function waitForPath(window) {
  for (let attempt = 0; attempt < 80; attempt++) {
    const ready = await window.webContents.executeJavaScript(
      `document.getElementById('sim-status')?.textContent?.includes('Path ready')`,
    );
    if (ready) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('Academy path never became ready');
}

async function waitForOnboarding(window) {
  for (let attempt = 0; attempt < 80; attempt++) {
    const ready = await window.webContents.executeJavaScript(
      `Boolean(document.getElementById('onboarding-ftc'))`,
    );
    if (ready) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('Academy onboarding never appeared');
}

app.whenReady().then(async () => {
  const window = new BrowserWindow({
    show: false,
    webPreferences: { backgroundThrottling: false },
  });
  try {
    await window.loadURL(url);
    await window.webContents.executeJavaScript(
      `localStorage.removeItem('academy-progress-v4'); localStorage.setItem('academy-progress-v5-ftc','[]'); location.reload()`,
    );
    await waitForOnboarding(window);
    await window.webContents.executeJavaScript(`document.getElementById('onboarding-ftc').click()`);
    await waitForPath(window);
    const pointerResult = await window.webContents.executeJavaScript(`(async () => {
      const button = document.getElementById('hold-drive');
      const readout = document.getElementById('coordinate-readout');
      const before = readout.textContent;
      button.dispatchEvent(new PointerEvent('pointerdown', {
        bubbles: true, cancelable: true, pointerId: 41, pointerType: 'mouse', button: 0,
      }));
      await new Promise(resolve => setTimeout(resolve, 1200));
      const during = readout.textContent;
      const status = document.getElementById('sim-status').textContent;
      button.dispatchEvent(new PointerEvent('pointerup', {
        bubbles: true, cancelable: true, pointerId: 41, pointerType: 'mouse', button: 0,
      }));
      return { before, during, status };
    })()`);
    if (pointerResult.before === pointerResult.during) {
      throw new Error(`pointer hold did not move robot: ${JSON.stringify(pointerResult)}`);
    }
    await window.webContents.executeJavaScript(`document.getElementById('reset-run').click()`);
    await new Promise(resolve => setTimeout(resolve, 100));
    const beforeSpace = await window.webContents.executeJavaScript(
      `document.getElementById('coordinate-readout').textContent`,
    );
    window.webContents.sendInputEvent({ type: 'keyDown', keyCode: 'Space' });
    await new Promise(resolve => setTimeout(resolve, 1200));
    const afterSpace = await window.webContents.executeJavaScript(
      `document.getElementById('coordinate-readout').textContent`,
    );
    window.webContents.sendInputEvent({ type: 'keyUp', keyCode: 'Space' });
    if (beforeSpace === afterSpace) {
      throw new Error(`space hold did not move robot: ${beforeSpace}`);
    }
    console.log(`Pointer hold passed: ${pointerResult.before} -> ${pointerResult.during}`);
    console.log(`Space hold passed: ${beforeSpace} -> ${afterSpace}`);
    app.exit(0);
  } catch (error) {
    console.error(error);
    app.exit(1);
  }
});
