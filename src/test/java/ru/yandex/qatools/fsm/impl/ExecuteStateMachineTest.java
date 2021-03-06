package ru.yandex.qatools.fsm.impl;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.StopConditionAware;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov
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
    public abstract class ExecuteStateMachine implements StopConditionAware<ExecuteState, Object> {
        @OnTransit
        public abstract void onProcessStarted(Idle from, Running to, ProcessStarted event);

        @OnTransit
        public abstract void onProcessTerminate(Running from, TerminateProcess event);

        @OnTransit
        public abstract void onProcessStartedAtCancelling(Cancelling from, ProcessStarted event);

        @OnTransit
        public abstract void onProcessTerminatedAtCancelling(ExecuteState from, ProcessTerminated event);

        @NewState
        public ExecuteState initState(Class<? extends ExecuteState> state, TestState event) throws IllegalAccessException, InstantiationException {
            return state.newInstance();
        }

        @Override
        public boolean isStopRequired(ExecuteState state, Object event) {
            return false;
        }
    }

    private ExecuteStateMachine fsm;
    private YatomataImpl engine;

    @Before
    public void init() throws FSMException, InstantiationException, IllegalAccessException {
        fsm = mock(ExecuteStateMachine.class);
        when(fsm.isStopRequired(any(ExecuteState.class), any())).thenCallRealMethod();
        when(fsm.initState(any(Class.class), any(TestState.class))).thenCallRealMethod();
        engine = new YatomataImpl<>(ExecuteStateMachine.class, fsm);
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
    public void testNoTransition() throws InstantiationException, IllegalAccessException {
        ExecuteState state = (ExecuteState) engine.fire(new ProcessStarted());
        verify(fsm).onProcessStarted(any(Idle.class), any(Running.class), any(ProcessStarted.class));
        assertTrue("State must be Running", state instanceof Running);
        assertTrue("State must be Running", engine.fire(new Object()) instanceof Running);
        verify(fsm).initState(any(Class.class), any(ProcessStarted.class));
        verify(fsm).isStopRequired(any(Running.class), any(ProcessStarted.class));
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
