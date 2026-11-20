// Shows why the backend didn't start, if the Electron app passed an error in the URL.
const params = new URLSearchParams(location.search);
const error = params.get('error');
if (params.get('academy') === '1') {
  document.title = 'Path Planner Academy';
  document.getElementById('app-name').textContent = 'Path Planner Academy';
}
if (error) {
  document.getElementById('status').textContent = "Couldn't start the simulator";
  document.getElementById('spinner').hidden = true;
  const pre = document.getElementById('error');
  pre.textContent = error + '\n\nTry running ./gradlew :FieldViewer:run in the repo to see the full error.';
  pre.hidden = false;
}
