package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ExecuteStateMachineTest {

    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = ProcessStarted.class, to = Running.class),
            @Transit(from = Idle.class, on = TerminateProcess.class, to = Cancelling.class),
            @Transit(from = {Idle.class, Cancelling.class}, on = ProcessTerminated.class, stop = true),
            @Transit(from = Cancelling.class, on = ProcessStarted.class, stop = true),
            @Transit(from = Running.class, on = {ProcessCompleted.class, ProcessFailed.class, ProcessTerminated.class}, stop = true),
            @Transit(from = Running.class, on = TerminateProcess.class)
    })
    public interface ExecuteStateMachine {
        @OnTransit
        public void onProcessStarted(Idle from, Running to, ProcessStarted event);

        @OnTransit
        public void onProcessTerminate(Running from, TerminateProcess event);

        @OnTransit
        public void onProcessStartedAtCancelling(Cancelling from, ProcessStarted event);

        @OnTransit
        public void onProcessTerminatedAtCancelling(ExecuteState from, ProcessTerminated event);
    }

    private ExecuteStateMachine fsm;
    private YatomataImpl engine;

    @Before
    public void init() {
        fsm = mock(ExecuteStateMachine.class);
        engine = new YatomataImpl(ExecuteStateMachine.class, fsm);
    }

    @Test
    public void testProcessTerminated() {
        final ProcessTerminated evtTerminated = new ProcessTerminated();

        ExecuteState state = (ExecuteState) engine.fire(evtTerminated);
        assertTrue("State must be Idle", state instanceof Idle);
        verify(fsm).onProcessTerminatedAtCancelling(any(Running.class), same(evtTerminated));
        assertTrue("State must be stopped", engine.isCompleted());
    }

    @Test
    public void testProcessTerminateAtRunning() {
        final ProcessStarted evtStarted = new ProcessStarted();
        final TerminateProcess evtTerminateProc = new TerminateProcess();

        ExecuteState state = (ExecuteState) engine.fire(evtStarted);
        assertTrue("State must be Running", state instanceof Running);
        engine.fire(evtTerminateProc);
        verify(fsm).onProcessTerminate(any(Running.class), same(evtTerminateProc));
        assertFalse("State must not be stopped", engine.isCompleted());
    }

    @Test
    public void testProcessStartedAtCancelling() {
        final TerminateProcess evtTerminateProc = new TerminateProcess();
        final ProcessStarted evtProcessStarted = new ProcessStarted();

        ExecuteState state = (ExecuteState) engine.fire(evtTerminateProc);
        assertTrue("State must be Cancelling", state instanceof Cancelling);
        engine.fire(evtProcessStarted);
        verify(fsm).onProcessStartedAtCancelling(any(Cancelling.class), same(evtProcessStarted));
        assertTrue("State must be stopped", engine.isCompleted());
    }

    @Test
    public void testNoTransition() {
        ExecuteState state = (ExecuteState) engine.fire(new ProcessStarted());
        verify(fsm).onProcessStarted(any(Idle.class), any(Running.class), any(ProcessStarted.class));
        assertTrue("State must be Running", state instanceof Running);
        assertTrue("State must be Running", engine.fire(new Object()) instanceof Running);
        verifyNoMoreInteractions(fsm);
    }

    @Test
    public void testNullEvent() {
        ExecuteState state = (ExecuteState) engine.fire(null);
        assertTrue("State must be Idle", state instanceof Idle);
        verifyNoMoreInteractions(fsm);
    }

    @Test(expected = StateMachineException.class)
    public void testStoppedThrowsError() {
        engine.fire(new ProcessTerminated());
        engine.fire(new ProcessStarted());
    }

    @Test
    public void testStartedCompleted() {
        final ProcessStarted evtStarted = new ProcessStarted();
        final ProcessCompleted evtCompleted = new ProcessCompleted();

        ExecuteState state = (ExecuteState) engine.fire(evtStarted);
        assertTrue("State must be running", state instanceof Running);
        verify(fsm).onProcessStarted(any(Idle.class), any(Running.class), same(evtStarted));
        state = (ExecuteState) engine.fire(evtCompleted);
        assertTrue("State still must be running", state instanceof Running);
        assertTrue("State must be stopped", engine.isCompleted());
    }
}
