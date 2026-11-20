const { app, BrowserWindow } = require('electron');

const url = process.env.ACADEMY_TEST_URL || 'http://localhost:2526/academy/index.html';

async function waitFor(window, expression, message, attempts = 120) {
  for (let attempt = 0; attempt < attempts; attempt++) {
    if (await window.webContents.executeJavaScript(expression)) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error(message);
}

app.whenReady().then(async () => {
  const window = new BrowserWindow({
    show: false,
    webPreferences: { backgroundThrottling: false },
  });
  try {
    await window.loadURL(url);
    await window.webContents.executeJavaScript(
      `localStorage.setItem('academy-progress-v5-ftc', JSON.stringify([0,1,2,3,4,5])); location.reload()`,
    );
    await waitFor(window, `Boolean(document.getElementById('onboarding-ftc'))`,
      'FTC onboarding choice did not appear');
    await window.webContents.executeJavaScript(`document.getElementById('onboarding-ftc').click()`);
    await waitFor(window, `Boolean(document.querySelector('[data-lesson="6"]'))`,
      'Final lesson did not unlock');
    await window.webContents.executeJavaScript(`document.querySelector('[data-lesson="6"]').click()`);
    await waitFor(window, `document.getElementById('sim-status')?.textContent?.includes('Add one more')`,
      'Final lesson did not load');
    await window.webContents.executeJavaScript(`(() => {
      const canvas = document.getElementById('academy-field');
      const rect = canvas.getBoundingClientRect();
      canvas.dispatchEvent(new PointerEvent('pointerup', {
        bubbles: true, cancelable: true, pointerId: 51, pointerType: 'mouse', button: 0,
        clientX: rect.left + rect.width * 0.20,
        clientY: rect.top + rect.height * 0.80,
      }));
    })()`);
    try {
      await waitFor(window, `document.getElementById('sim-status')?.textContent?.includes('Path ready')`,
        'Final route was not planned');
    } catch (error) {
      const status = await window.webContents.executeJavaScript(
        `document.getElementById('sim-status')?.textContent`,
      );
      throw new Error(`${error.message}: ${status}`);
    }
    await window.webContents.executeJavaScript(`document.getElementById('hold-drive').dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, cancelable: true, pointerId: 52, pointerType: 'mouse', button: 0 })
    )`);
    await waitFor(window, `document.getElementById('completion-dialog')?.open === true`,
      'Graduation summary did not open after the final mission');
    const summary = await window.webContents.executeJavaScript(`({
      title: document.getElementById('completion-title').textContent,
      hasMechanisms: document.querySelector('.mechanism-note').textContent.includes('shooter'),
      hasQuestionLink: document.querySelector('.question-card a[href*="/contact-us"]') !== null,
      stars: document.getElementById('star-count').textContent,
    })`);
    if (!summary.hasMechanisms || !summary.hasQuestionLink || summary.stars !== '7') {
      throw new Error(`Graduation summary is incomplete: ${JSON.stringify(summary)}`);
    }
    console.log(`Completion dialog passed: ${summary.title} (${summary.stars}/7 stars)`);
    app.exit(0);
  } catch (error) {
    console.error(error);
    app.exit(1);
  }
});
