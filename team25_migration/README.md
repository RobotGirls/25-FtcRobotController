# Team 25 purple FTC simulator migration

This package produces a modified copy of the `FieldViewer` simulator from `issue-38-purePursuitTesting`, directly inside **your local checkout** of `issue-39-purePursuitStimulatorTesting`. It downloads the original files from the official RobotGirls repository on your Mac. The package itself is a builder, **not** the finished simulator source ZIP.

## Mac setup with GitHub Desktop

1. Clone `RobotGirls/25-FtcRobotController` in GitHub Desktop if you haven't already.
2. Select branch `issue-39-purePursuitStimulatorTesting` (don't use `issue-38`).
3. Make sure GitHub Desktop shows **no uncommitted changes**.
4. In GitHub Desktop, choose **Repository → Show in Finder** and note the repository folder.
5. Open Terminal, and run (replace the sample folder path with your own):

   ```bash
   python3 /path/to/unzipped/team25_migration/build_team25_simulator.py "/path/to/your/25-FtcRobotController"
   ```

   **Tip:** In Terminal, type `python3 `, drag `build_team25_simulator.py` into Terminal, type a space, drag the repository folder into Terminal, then press Return.

6. The script downloads the exact branch source and copies `FieldViewer/` and `../TeamCode/PurePursuit/`, adds the field background resource if missing, and updates `settings.gradle` only to register those modules. It won't modify unrelated robot code.
7. **Review the changed files in GitHub Desktop before committing.** If everything looks right, commit to the `issue-39` branch and click **Push origin**.

## Test the simulator

From the repository root:

```bash
cd FieldViewer/web
npm install
npm start
```

You need Node.js 18+ and a working Java/Gradle environment. The app uses a Java backend and Electron UI. Direct backend browser mode uses `http://localhost:2525`.

## What was changed?

- Team identifier `5218` → `25` inside the copied simulator modules, including Java `org.team5218` → `org.team25` and its directory.
- The old port `5218` → `2525` because port 25 is a privileged mail-server port, not an appropriate local simulator port.
- Original simulator stylesheet is preserved, with a purple theme overlay added at the end (lavender accents, dark-violet backgrounds, purple logo).
- `PurePursuit` stays a separate shared library module; existing robot code is NOT overwritten.

## Notes

- The migration has not been executed or build-tested in this environment because direct downloads from GitHub aren't available here.
- If the source contains other uses of numeric `5218` as a port or protocol constant, inspect them during your review.
- The script aborts on a dirty target checkout, an incorrect branch, or an already-present simulator module to protect existing work.
- No commits or pushes are made by the script.
