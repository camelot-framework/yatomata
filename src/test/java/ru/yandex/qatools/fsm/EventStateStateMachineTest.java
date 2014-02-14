package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.beans.UndefinedEvent;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @author: Ilya Sadykov
 */

public class EventStateStateMachineTest {

    @FSM(start = UndefinedEvent.class, initStateMethod = "initState")
    @Transitions({
            @Transit(from = UndefinedEvent.class, to = TestStarted.class, on = TestStarted.class),
            @Transit(from = TestStarted.class, to = TestFailed.class, on = TestFailed.class)
    })
    public interface EventStateStateMachine {
        public Object initState(Class<? extends TestEvent> stateClass, TestEvent event);

        @OnTransit
        public void onTestFailed(TestStarted oldState, TestFailed newState, TestFailed event);
    }

    EventStateStateMachine fsm;
    Yatomata<EventStateStateMachine> engine;

    @Before
    public void init() {
        fsm = mock(EventStateStateMachine.class);
        when(fsm.initState(eq(UndefinedEvent.class), (TestEvent) eq(null))).thenReturn(new UndefinedEvent());
        when(fsm.initState(eq(TestStarted.class), any(TestStarted.class))).thenReturn(new TestStarted());
        when(fsm.initState(eq(TestFailed.class), any(TestFailed.class))).thenReturn(new TestFailed());
        engine = new YatomataImpl(EventStateStateMachine.class, fsm);
    }

    @Test
    public void testInitStateMethod() {
        final TestStarted testStarted = new TestStarted();
        final TestFailed testFailed = new TestFailed();
        engine.fire(testStarted);
        engine.fire(testFailed);
        verify(fsm).initState(same(UndefinedEvent.class), (TestEvent) eq(null));
        verify(fsm).initState(same(TestStarted.class), same(testStarted));
        verify(fsm).initState(same(TestFailed.class), same(testFailed));
    }
}
