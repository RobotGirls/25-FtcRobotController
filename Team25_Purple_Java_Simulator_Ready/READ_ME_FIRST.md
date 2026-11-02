# Team 25 Purple FieldViewer — ready-to-copy files

This download contains the actual Java-based simulator and its TypeScript/Electron desktop interface, converted from issue-38. **No Python script needs to be run.**

## Copy into your project
1. In GitHub Desktop, select `issue-39-purePursuitStimulatorTesting` and **Repository > Show in Finder**. Make sure you have committed or stashed any existing changes.
2. Unzip this archive.
3. Drag `FieldViewer` and `PurePursuit` into the **root** of your Android Studio repository (beside `TeamCode`, NOT inside it).
4. Drag the included `MeepMeepTesting/src/main/resources/biobuzz-field.png` into the same relative location in your repository. The target branch already has `MeepMeepTesting`, but may not have this resource folder.
5. Open your existing root `settings.gradle`, and add these two lines after the current `include` statements (do not replace the file):

```gradle
include ':PurePursuit'
include ':FieldViewer'
```

6. Sync Gradle in Android Studio. These are desktop/Java modules, not Android OpModes, so don't look for them in the OpMode list.
7. To open the graphical application, open Terminal in your repository root and run:

```bash
cd FieldViewer/web
npm install
npm start
```

Node.js 18+ is required for the Electron interface. The Java backend is built automatically by the app using your repository Gradle wrapper. If you want browser mode instead, first run `npm run build` in `FieldViewer/web`, then from the repository root run `./gradlew :FieldViewer:run`, and open http://localhost:2525.

8. Review changes in GitHub Desktop, then commit and push to `issue-39` when satisfied.

## What was changed
- Every textual `5218` in the two modules was replaced with `25` (including Java package path `org/team25/fieldviewer`).
- Server and Electron port defaults were aligned at `2525` (port 25 would require elevated privileges on some machines).
- UI backgrounds, interactive accents, logo, loading animation, and field display primary path colors updated to purple/lavender.
- `PurePursuit` logic was otherwise preserved.
- Field image bundled as a separate dependency.

## Important
- The package was checked for consistency, but was **not runtime/build-tested** in Android Studio or Electron in this environment.
- Do not copy the whole source branch over your target: the only directories to merge are those provided in this ZIP.
- `settings.gradle` is deliberately NOT included because replacing it would overwrite your target branch's configuration.
