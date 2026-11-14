package org.firstinspires.ftc.teamcode.purepursuit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.acmerobotics.roadrunner.Pose2d;

import org.junit.Test;

import java.util.Collections;

public class MechanismBindingsTest {
    @Test
    public void everyMarkerStepGetsAFreshCommand() {
        MechanismBindings<Object> bindings = new MechanismBindings<>().register("shoot", Object::new);
        assertNotSame(bindings.create("shoot"), bindings.create("shoot"));
    }

    @Test
    public void findsMarkersWithNothingRegistered() {
        Route route = Route.builder("r", Route.Alliance.NONE, new Pose2d(0, -48, 0))
                .waitSeconds(1)
                .marker("shoot", 2)
                .parallelMarker("intaek", 2)
                .marker("shoot", 2)
                .build();
        MechanismBindings<Object> bindings = new MechanismBindings<>().register("shoot", Object::new);
        assertEquals(Collections.singletonList("intaek"), bindings.unbound(route));
    }

    @Test
    public void bindsSerialAndParallelMarkers() {
        MechanismBindings<Object> bindings = new MechanismBindings<>()
                .register("shoot", Object::new)
                .register("intake", Object::new);
        Route route = Route.builder("mechanisms", Route.Alliance.NONE, new Pose2d(0, -48, 0))
                .parallelMarker("intake", 2)
                .marker("shoot", 1)
                .build();
        assertEquals(Collections.emptyList(), bindings.unbound(route));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aMarkerCanOnlyBeRegisteredOnce() {
        new MechanismBindings<>().register("shoot", Object::new).register("shoot", Object::new);
    }
}
