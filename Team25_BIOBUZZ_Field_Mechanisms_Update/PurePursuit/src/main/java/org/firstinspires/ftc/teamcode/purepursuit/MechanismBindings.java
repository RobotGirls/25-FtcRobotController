package org.firstinspires.ftc.teamcode.purepursuit;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Connect simulation marker names to the real robot's separate mechanism classes.
 *
 * <p>This core module stays platform independent. On Android, instantiate this with
 * Road Runner Action as T and register factories from your Intake, Shooter, etc classes.
 * Markers retain their estimated durations ONLY for simulation; execution uses the
 * Action returned by the factory and finishes based on the real mechanism.
 *
 * @param <T> robot command type, e.g. com.acmerobotics.roadrunner.Action
 */
public final class MechanismBindings<T> {
    private final Map<String, Supplier<? extends T>> commands = new LinkedHashMap<>();

    /** Registers a marker with a factory that creates a FRESH command each time. */
    public MechanismBindings<T> register(String markerName, Supplier<? extends T> factory) {
        if (markerName == null || markerName.trim().isEmpty() || factory == null) {
            throw new IllegalArgumentException("Marker name and mechanism factory are required");
        }
        if (commands.containsKey(markerName)) {
            throw new IllegalArgumentException("Already registered marker: " + markerName);
        }
        commands.put(markerName, factory);
        return this;
    }

    /** Converts a simulated marker to a robot command, or throws on an unknown marker. */
    public T create(Route.Marker marker) {
        if (marker == null) throw new IllegalArgumentException("marker is required");
        Supplier<? extends T> factory = commands.get(marker.name);
        if (factory == null) {
            throw new IllegalArgumentException("No mechanism bound for: " + marker.name);
        }
        T command = factory.get();
        if (command == null) {
            throw new IllegalStateException("Mechanism factory returned null: " + marker.name);
        }
        return command;
    }

    /** Materialize commands for an existing PurePursuitAutoBase.markers() override. */
    public Map<String, T> createAll() {
        Map<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, Supplier<? extends T>> entry : commands.entrySet()) {
            T command = entry.getValue().get();
            if (command == null) {
                throw new IllegalStateException("Mechanism factory returned null: " + entry.getKey());
            }
            result.put(entry.getKey(), command);
        }
        return Collections.unmodifiableMap(result);
    }

    public boolean supports(String markerName) {
        return commands.containsKey(markerName);
    }
}
