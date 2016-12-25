package ru.yandex.qatools.fsm.impl;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.*;

import static org.mockito.Mockito.*;

public class AnnotatedMethodParametersTest {

    @FSM(start = TestStartedState.class)
    @Transitions({
            @Transit(from = TestStartedState.class, to = TestPassedState.class, on = TestPassed.class),
            @Transit(from = TestStartedState.class, to = TestFailedState.class, on = TestFailed.class),
    })
    public interface CorrectlyAnnotatedFSM {

        @OnTransit
        void onTestStarted(
                @FromState TestStartedState fromState,
                @Event TestPassed event,
                @ToState TestPassedState testPassedState
        );

        @OnTransit
        void onTestFailed(@Event TestFailed event);

        @OnException
        void onException(Throwable e, @FromState TestStartedState state);
    }

    @FSM(start = TestStartedState.class)
    @Transitions({
            @Transit(from = TestStartedState.class, to = TestPassedState.class, on = TestPassed.class)
    })
    public interface IncorrectlyAnnotatedFSM {

        //Two annotations on the same parameter
        @OnTransit
        void onTestStarted(@FromState @ToState TestStartedState fromState);

    }

    private CorrectlyAnnotatedFSM fsm;
    private Yatomata<CorrectlyAnnotatedFSM> engine;

    @Before
    public void init() throws FSMException {
        fsm = mock(CorrectlyAnnotatedFSM.class);
        engine = new YatomataImpl<>(CorrectlyAnnotatedFSM.class, fsm);
    }

    @Test
    public void testOnTransitAllParameters() {
        TestPassed event = new TestPassed();
        engine.fire(event);
        verify(fsm).onTestStarted(any(TestStartedState.class), eq(event), any(TestPassedState.class));
        verifyNoMoreInteractions(fsm);
    }

    @Test
    public void testOnTransitAlSomeParameters() {
        TestFailed event = new TestFailed();
        engine.fire(event);
        verify(fsm).onTestFailed(eq(event));
        verifyNoMoreInteractions(fsm);
    }

    @Test
    public void testOnException() {
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(fsm).onTestFailed(any(TestFailed.class));
        TestFailed event = new TestFailed();
        engine.fire(event);
        verify(fsm).onTestFailed(eq(event));
        verify(fsm).onException(eq(exception), any(TestStartedState.class));
        verifyNoMoreInteractions(fsm);
    }

    @Test(expected = StateMachineException.class)
    public void testMultipleAnnotationsOnTheSameParameter() throws FSMException {
        IncorrectlyAnnotatedFSM fsm = mock(IncorrectlyAnnotatedFSM.class);
        Yatomata<IncorrectlyAnnotatedFSM> engine = new YatomataImpl<>(IncorrectlyAnnotatedFSM.class, fsm);
        engine.fire(new TestPassed());
    }
}
