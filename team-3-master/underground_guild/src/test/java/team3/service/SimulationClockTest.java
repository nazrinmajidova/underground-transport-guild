package team3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimulationClockTest {

    @Test
    void tickUsesConfiguredSpeedMultipliers() {
        SimulationClock clock = new SimulationClock();

        clock.tick();
        assertEquals(1.0f, clock.getElapsedTime());

        clock.setSpeed("fast");
        clock.tick();
        assertEquals(3.0f, clock.getElapsedTime());

        clock.setSpeed("very_fast");
        clock.tick();
        assertEquals(7.0f, clock.getElapsedTime());
    }

    @Test
    void pauseStopsElapsedTimeAndResumeRestoresPreviousSpeed() {
        SimulationClock clock = new SimulationClock();

        clock.setSpeed("fast");
        clock.pause();
        clock.tick();

        assertEquals("pause", clock.getSpeed());
        assertEquals(0.0f, clock.getElapsedTime());
        assertEquals(0, clock.getTotalTicks());

        clock.resume();
        clock.tick();

        assertEquals("fast", clock.getSpeed());
        assertEquals(2.0f, clock.getElapsedTime());
        assertEquals(1, clock.getTotalTicks());
    }
}
