const { app, BrowserWindow } = require('electron');

const url = process.env.ACADEMY_TEST_URL || 'http://localhost:2526/academy/index.html';

async function waitFor(window, expression, message, attempts = 100) {
  for (let attempt = 0; attempt < attempts; attempt++) {
    if (await window.webContents.executeJavaScript(expression)) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error(message);
}

app.whenReady().then(async () => {
  const window = new BrowserWindow({ show: false, webPreferences: { backgroundThrottling: false } });
  try {
    await window.loadURL(url);
    await window.webContents.executeJavaScript(
      `localStorage.setItem('academy-progress-v5-new','[]'); location.reload()`,
    );
    await waitFor(window, `document.getElementById('onboarding-dialog')?.open === true`,
      'Onboarding did not open before the Academy');
    await window.webContents.executeJavaScript(`document.getElementById('onboarding-new').click()`);
    const intro = await window.webContents.executeJavaScript(`({
      title: document.getElementById('lesson-title').textContent,
      total: document.getElementById('star-total').textContent,
      quizVisible: Boolean(document.querySelector('.first-quiz')),
    })`);
    if (intro.title !== 'Meet FIRST and FTC' || intro.total !== '8' || !intro.quizVisible) {
      throw new Error(`Beginner path did not start correctly: ${JSON.stringify(intro)}`);
    }
    await window.webContents.executeJavaScript(`document.querySelector('[data-action="quiz-answer-0"]').click()`);
    const stayedLocked = await window.webContents.executeJavaScript(
      `document.getElementById('complete-lesson').disabled`,
    );
    if (!stayedLocked) throw new Error('Wrong FIRST answer unlocked the next mission');
    await window.webContents.executeJavaScript(`document.querySelector('[data-action="quiz-answer-1"]').click()`);
    await waitFor(window, `document.querySelector('.quiz-progress').textContent.includes('2 OF 3')`,
      'Second FIRST question did not appear');
    await window.webContents.executeJavaScript(`document.querySelector('[data-action="quiz-answer-0"]').click()`);
    await waitFor(window, `document.querySelector('.quiz-progress').textContent.includes('3 OF 3')`,
      'Third FIRST question did not appear');
    await window.webContents.executeJavaScript(`document.querySelector('[data-action="quiz-answer-2"]').click()`);
    await waitFor(window, `document.getElementById('star-count').textContent === '1'`,
      'Correct FIRST answer did not complete the welcome mission');
    await window.webContents.executeJavaScript(`document.getElementById('complete-lesson').click()`);
    await waitFor(window, `document.getElementById('lesson-title').textContent === 'Give the robot a destination'`,
      'Beginner path did not continue to the waypoint mission');
    console.log('FTC/new choice and beginner FIRST quiz passed');
    app.exit(0);
  } catch (error) {
    console.error(error);
    app.exit(1);
  }
});
