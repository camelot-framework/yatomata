package ru.yandex.qatools.fsm.impl;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov
 */
public class OnExceptionStateMachineTest {

    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = ProcessStarted.class, to = Running.class),
            @Transit(from = Running.class, on = ProcessCompleted.class, to = Idle.class),
    })
    public class OnExceptionStateMachine {
        @OnTransit
        public void onStart(Idle from, Running to, ProcessStarted event) {
            throw new ArithmeticException("Failed to calculate some");
        }

        @OnTransit
        public void onCancel(Running from, Idle to, ProcessCompleted event) {
            throw new RuntimeException("Failure");
        }

        @OnException(preserve = true)
        public void onArithmeticException(ArithmeticException e, ExecuteState state) {

        }

        @OnException
        public void onRuntimeException(RuntimeException e, ExecuteState state) {

        }
    }

    private YatomataImpl engine;
    OnExceptionStateMachine fsm = mock(OnExceptionStateMachine.class);

    @Before
    public void init() throws FSMException {
        doCallRealMethod().when(fsm).onStart(any(Idle.class), any(Running.class), any(ProcessStarted.class));
        doCallRealMethod().when(fsm).onCancel(any(Running.class), any(Idle.class), any(ProcessCompleted.class));
        engine = new YatomataImpl<>(OnExceptionStateMachine.class, fsm);
    }

    @Test
    public void testStateMustBeChangedAfterNotCriticalException() {
        assertThat("State must be changed to Running", engine.fire(new ProcessStarted()), instanceOf(Running.class));
        assertThat("State must remain Running", engine.fire(new ProcessCompleted()), instanceOf(Running.class));

        verify(fsm).onStart(any(Idle.class), any(Running.class), any(ProcessStarted.class));
        verify(fsm).onArithmeticException(any(ArithmeticException.class), any(Idle.class));
        verify(fsm).onCancel(any(Running.class), any(Idle.class), any(ProcessCompleted.class));
        verify(fsm).onRuntimeException(any(ArithmeticException.class), any(Running.class));
    }
}
