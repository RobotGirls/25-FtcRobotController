# FTC Android integration (adapter, NOT ready-to-run OpMode)

Copy the six `.java` files in `src/main/java/org/ftc/hiveplanner/` into
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/hiveplanner/` and change their
`package` declarations to match, OR retain the original package and import it.

**Set up verified values (illustrative API, values deliberately omitted):**

```java
FieldConfig config = new FieldConfig(
    YOUR_MEASURED_BOUNDING_RADIUS_IN,
    YOUR_EXTRA_MARGIN_IN,
    YOUR_PERIMETER_BUFFER_IN,
    VERIFIED_HIVE_CENTER_X_IN,
    VERIFIED_HIVE_CENTER_Y_IN,
    VERIFIED_HIVE_ALL_STATES_EXCLUSION_RADIUS_IN,
    3.0,
    true // ONLY after a real field/robot survey & validation
);
FieldMap map = new FieldMap(config);
AStarPlanner planner = new AStarPlanner(map, config.gridIn);
GoalSelector selector = new GoalSelector(planner);
List<Point> allianceGoals = Arrays.asList(
    new Point(VERIFIED_GOAL_1_X, VERIFIED_GOAL_1_Y),
    new Point(VERIFIED_GOAL_2_X, VERIFIED_GOAL_2_Y));
DriveAssist assist = new DriveAssist(robotAdapter, selector, config, allianceGoals);
```

Implement `DriveAssist.Robot` using *your own* `Pose2d`/odometry and Pure Pursuit
APIs; implement `followPath` so it receives `List<Point>` and constructs your
controller's waypoint/path format. Translate inches to your localization units.

```java
// In OpMode loop: manual joystick input must also be handled by your adapter.
assist.update(gamepad1.y, gamepad1.b);
if (!assist.active()) {
    // your normal manual drive code
}
```

Important integration notes:
- `followPath` MUST be nonblocking; `updateFollower` advances it each loop.
- Your adapter's `manualOverrideRequested()` should detect significant stick input.
- `cancelFollowerAndStop` MUST stop drivetrain motion immediately.
- Verify fresh/reliable localization before and during auto assist.
- Pure Pursuit arc/curve smoothing can leave the straight-line corridor. Validate its
  actual generated trajectory, not only the planner's waypoints.
- A `true` survey flag is not proof of safety; perform supervised physical tests.
- Include timeout and no-progress watchdog before competition deployment.
- This planner does not dynamically avoid opponents or teammates.


## Alliance selection (required)

During `init`, provide an explicit alliance selection (e.g. gamepad D-pad) and show it in telemetry. Do **not** silently infer the alliance from location.

```java
Alliance selectedAlliance = Alliance.RED; // update during init from driver selection
AllianceGoals goals = new AllianceGoals(redVerifiedGoals, blueVerifiedGoals);
DriveAssist assist = new DriveAssist(adapter, selector, config, goals, selectedAlliance);
// after selection and before start:
assist.setAlliance(selectedAlliance);
// inside loop:
assist.update(gamepad1.y, gamepad1.b);
```

`redVerifiedGoals` and `blueVerifiedGoals` must use the same odometry coordinate frame, include the intended shooting heading in your downstream pursuit/aiming controller, and be verified for both HIVE states. The sample contains no certified destination coordinates.
