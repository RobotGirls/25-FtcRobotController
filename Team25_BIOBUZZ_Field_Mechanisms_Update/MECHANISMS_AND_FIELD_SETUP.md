# Team 25 purple simulator — field accuracy and mechanisms

## Field artwork and orientation

- `FieldViewer/src/main/resources/biobuzz-field.png` is now INCLUDED with the viewer itself.
- The purple field processor keeps four gold FLOWERS, two center HIVES and red/blue alliance markings visible instead of flattening them to a uniform dark grid.
- Field coordinates: center `(0,0)`, **+X right**, **+Y up**. Default view: **BLUE HIVE above center**, **RED HIVE below center**. Use **O** or Rotate in the viewer only to rotate the view; underlying pose coordinates do not change.
- The image is a 2D season illustration, not a dimensionally certified collision/AprilTag model. The current center keep-out box remains an approximation; measure against the current manual and your robot for safe autonomous use.
- Verify at startup that `http://localhost:2525/field-dark.png` displays actual Hives and Flowers. If not, restart the backend to load the new resource.

## Install without disturbing your robot code

1. Check out `issue-39-purePursuitStimulatorTesting` in GitHub Desktop.
2. Copy **the contents** of `FieldViewer/` and `PurePursuit/` from this package into the folders with those names at the repository root; merge/overwrite ONLY these module files.
3. Keep your existing `TeamCode` and its mechanism classes unchanged. The Gradle modules should already be included in `settings.gradle`.
4. From repository root run `./gradlew :PurePursuit:test :FieldViewer:classes`. The desktop viewer must depend on the **plain Java PurePursuit** module. If you have converted PurePursuit to an Android library, see warning below.
5. From `FieldViewer/web/`, run `npm install` (first time) and `npm start` to open the viewer. Commit after testing.

**Android library warning:** A desktop `application` module cannot normally depend directly on an Android-library AAR variant using `implementation project(':PurePursuit')`. If you converted PurePursuit to `com.android.library`, restore its plain Java plugin for this desktop setup, or split the shared algorithms into a Java-only core module used by both Android and desktop. Do not overwrite working Robot Controller/TeamCode Gradle configuration.

## Use separate mechanism classes in Java autonomous routes

Your mechanism classes stay separate. Name each command once, add a `marker()` in the route, then map that marker to a real action factory in your OpMode.

**Route definition (pure Java, simulated on desktop):**

```java
Route auto = Route.builder("Blue auto", Route.Alliance.BLUE, startPose)
    .marker("intakeOn", 0.15)
    .follow(pathToCollect)
    .marker("intakeOff", 0.15)
    .follow(pathToShoot)
    .marker("shoot", 2.5)
    .build();
```

**Your repo already has `PurePursuitAutoBase` and it supports mechanisms via `markers()`!**
The `MechanismBindings` adapter is optional sugar over that API, so your mechanisms stay in
`TeamCode` and do not get copied into the desktop simulator.

Example matching your existing `TeamCode/.../biobuzz2627/auto/mechCode/Intake.java`:

```java
// Inside your subclass of PurePursuitAutoBase (in TeamCode):
private Intake intake;

@Override protected void initMechanisms() {
    intake = new Intake(hardwareMap);
}

@Override protected Map<String, Action> markers() {
    return new MechanismBindings<Action>()
        .register("collect", intake::closeClaw)
        .register("eject", intake::openClaw)
        .createAll();
}

// Your Route uses .marker("collect", 4.0), etc., at the corresponding place.
// Imports: java.util.Map; com.acmerobotics.roadrunner.Action;
// org.firstinspires.ftc.teamcode.purepursuit.MechanismBindings;
```

Your current `Intake.closeClaw()` and `openClaw()` names are from your source code, not new
methods invented for this example. **Important:** their `Action.run()` implementations use
`Thread.sleep()` for 4 and 2 seconds. That blocks the autonomous runner and is not a good
production pattern; refactor those classes to use elapsed time/state, and always stop motors
in `stopMechanisms()`, before running real hardware.

For your `ShooterPurePursuit` class, add an `Action`-returning method after implementing the necessary
logic and register it under the matching route marker name (e.g. `"shoot"`). Never map a
marker to a nonexistent method. In simulation, the estimated seconds are visualization only;
on the robot, your `Action` determines completion. `PurePursuitAutoBase` already supports
`markers()` and `stopMechanisms()`, so no robot API redesign is required.

Markers can be used for sequential actions between drive legs. Running mechanism actions in
parallel with a moving follower requires an explicit concurrent scheduling implementation;
this adapter does **not** imply that markers run during the drive segment.

`MechanismRouteExample.BLUE_DEMO` demonstrates creating an autonomous sequence with named mechanism markers, but is not enabled in the default route list, so no existing autonomous behavior changes.
