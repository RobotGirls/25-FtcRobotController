# BIOBUZZ HIVE TeleOp Path Planner — FTC Java Starter

**Status: functioning pure-Java planner plus a drive-only FTC test OpMode. Field geometry and shooting poses remain locked until the team verifies them.**

## Official measurements and provenance (as of 2026-11-02)

Source: **FIRST BIOBUZZ 2026–27 Competition Manual, Section 9 (TU06, Oct 22 2026)** and current **full Competition Manual TU07 (Oct 29 2026)**.

- Interior FIELD: approximately **144 x 144 inches** (Sec. 9.2)
- Tile size: approximately **24 x 24 inches** (Sec. 9.2)
- HIVE support FRAME width **49.46 in**, depth at base **38.95 in** (Sec. 9.6.1)
- HIVE pivot axis height **43.95 in** (Sec. 9.6.1)
- CELL opening approximately **20 in wide, 14 in tall, 12 in deep** (Sec. 9.6.2)
- FLOWER top opening approximately **21.5 in above tiles** (Sec. 9.7)
- General field tolerance **±1 inch** for nominal dimensions unless otherwise noted (Sec. 9.1)

Official source URLs:
- https://ftc-resources.firstinspires.org/ftc/game
- https://ftc-resources.firstinspires.org/ftc/game/manual-09
- https://ftc-resources.firstinspires.org/ftc/field

## Critical: what the manual does NOT determine for *your* robot

1. Robot bounding radius including mechanisms and any necessary dynamic margin.
2. Coordinate system alignment with your odometry/Pure Pursuit implementation.
3. A verified **floor-plane collision envelope** of the entire HIVE structure (frame, tipping cells, protrusions, both tilt states). The frame width/depth are *not* identical to the entire HIVE structure footprint.
4. FLOWER / perimeter protrusions in your actual event field and safe traversable area.
5. Actual safe scoring targets for your alliance, robot speed, and shooter calibration.
6. Other robots: static A* cannot sense them. Driver must watch and cancel.

`FieldConfig.simulationExample()` uses a 32-inch HIVE exclusion radius,
10-inch robot radius, and a centered HIVE as **UNVERIFIED EXAMPLE ASSUMPTIONS**.
These are deliberately marked `surveyedAndVerified=false`; `DriveAssist` will refuse
real operation until a qualified team member fills in surveyed geometry and explicitly
sets that flag. Do not simply flip the flag to make the code run.

## Alliance- and side-aware behavior

- The code now requires an explicit `Alliance.RED` or `Alliance.BLUE` choice.
- `AllianceGoals` holds separate, **surveyed and tested** shooting-position lists for red/blue and audience/far sides.
- Limelight detections are filtered to the selected official AprilTag cluster: red scoring IDs 30–33, red audience 34–37, blue audience 38–41, or blue scoring 42–45.
- A new path can begin only after a member of the selected cluster was seen recently. The generated path continues through a brief camera dropout because localization, not the camera image, follows the path.
- `DriveAssist` runs only while the assist button is held. Releasing it, pressing cancel, moving a drive stick, or losing localization stops the follower.
- Empty goal lists disable assist for that alliance/side combination.
- The HIVE obstacle geometry currently uses **one shared conservative exclusion model**; it must include both physical HIVE structures and their possible tipping envelopes if there are two distinct structures. Verify actual field layout before enabling movement.
- The manual's nominal frame dimensions do not tell you robot-appropriate shooting coordinates.

Example wiring (replace placeholders with team-verified values):

```java
Alliance selectedAlliance = Alliance.RED; // choose during init
AllianceGoals goals = new AllianceGoals(
        redAudienceGoals, redFarGoals, blueAudienceGoals, blueFarGoals);
DriveAssist assist = new DriveAssist(robotAdapter, selector, config, goals, selectedAlliance);
// In TeleOp loop:
assist.setSide(FieldSide.AUDIENCE);
assist.update(gamepad1.left_bumper, gamepad1.right_bumper);
```

## Driver controls in the test OpMode

Select `TEST PP Hive Shooting Assist` on the Driver Station. This OpMode initializes only the drivetrain, odometry, and Limelight; it does not initialize or command the shooter, intake, or servos.

- `X`: blue cluster filter
- `B`: red cluster filter
- D-pad left: audience-side cluster and shooting poses
- D-pad right: far/scoring-side cluster and shooting poses
- Hold left bumper: generate and follow the best safe route
- Release left bumper: stop path following
- Right bumper or drive-stick movement: cancel immediately

The dashboard/Driver Station telemetry shows the selected filter, matched tag, verification status, follower state, and field pose.

Before the first powered test, edit `HiveAssistConfig`, replace every estimated obstacle and shooting coordinate with measured values, verify the coordinate transform against Road Runner, and only then set `GEOMETRY_AND_GOALS_VERIFIED = true`. The default `false` value intentionally prevents autonomous movement.

### FTC class organization

- `PurePursuitHiveAssistTeleOp`: the single readable OpMode loop and telemetry.
- `ManualMecanumDriver`: ordinary gamepad driving and driver-override detection.
- `HiveTargetSelection`: color/side button state and AprilTag-cluster matching.
- `LimelightHiveTargetTracker`: camera lifecycle and selected-target freshness.
- `LimelightShootingRange`: selected-tag camera-space position, distance, angles, and calibrated shooting-window check.
- `HiveDriveAssistRobot`: Road Runner adapter for the hardware-independent state machine.
- `DynamicPurePursuitFollower`: nonblocking generated-path follower.
- `HiveAssistConfig`: dashboard-tunable geometry, speeds, and candidate shooting poses.

## How it fits together

- `FieldDimensions`: manual-derived nominal facts (inches).
- `FieldConfig`: team-specific safety dimensions (not supplied by FIRST).
- `FieldMap`: wall and central HIVE exclusion plus line-segment checking.
- `AStarPlanner`: 8-neighbor A* search with no corner cutting, validates edges,
  simplifies route using checked line-of-sight shortcuts.
- `GoalSelector`: chooses shortest valid path to a team-verified shooting candidate.
- `Alliance`, `FieldSide`, `AllianceGoals`: explicit color/side target selection with separate goal lists.
- `DriveAssist`: hold-to-run assist button, priority cancel, localization abort,
  non-blocking path follower interface.
- `FTC_INTEGRATION_EXAMPLE.md`: bridge to FTC SDK, your existing Pure Pursuit and FieldViewer.

## Compile and run pure-Java planner tests

Run from the repository root with JDK 8+:

```sh
./gradlew :HiveAutoPathPlanner:plannerTest
```

No FTC SDK, Gradle, or additional dependencies required for these tests.

## Before allowing robot navigation

- Verify ALL static obstacle envelopes in BOTH HIVE tilt states with official field CAD
  and local measurement, and add FLOWER obstacles or sufficient conservative boundary clearance.
- Fill `FieldConfig` with robot's real bounding geometry, a deliberate margin and
  verified HIVE center/exclusion radius (units inches).
- Match odometry axis origins, signs, and heading convention.
- Validate each final path segment and **any interpolated Pure Pursuit curves**.
  The provided checks verify *straight segments*, not arbitrary smoothed trajectories.
- Establish alliance-specific `verifiedGoals` with shooting tests; examples are not final.
- Integrate explicit cancellation and motor stop before field tests.
- Limit speed, test on a clear practice field, and never assume other robots are absent.
- Check that game rules and local referee guidance permit the planned route.
