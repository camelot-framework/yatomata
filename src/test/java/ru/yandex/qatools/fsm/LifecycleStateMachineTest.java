package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */

public class LifecycleStateMachineTest {

    @FSM(start = UndefinedState.class)
    @Transitions({
            @Transit(from = UndefinedState.class, to = TestStartedState.class, on = TestEvent.class),
            @Transit(from = TestStartedState.class, to = TestCompletedState.class, on = TestEvent.class)
    })
    public interface LifecycleStateMachine {
        @OnTransit
        public void onTestStart(UndefinedState oldState, TestStartedState newState, TestEvent event);

        @OnTransit
        public void onTestComplete(TestStartedState oldState, TestCompletedState newState, TestEvent event);
    }

    LifecycleStateMachine fsm;
    Yatomata<LifecycleStateMachine> engine;

    @Before
    public void init() {
        fsm = mock(LifecycleStateMachine.class);
        engine = new YatomataImpl(LifecycleStateMachine.class, fsm);
    }

    @Test
    public void testEventsReverse() {
        TestSkipped eventSkipped = new TestSkipped();
        TestStarted eventStarted = new TestStarted();
        assertTrue("Result must be test started state", engine.fire(eventSkipped) instanceof TestStartedState);
        assertTrue("Result must be test completed state", engine.fire(eventStarted) instanceof TestCompletedState);
        verify(fsm).onTestStart(any(UndefinedState.class), any(TestStartedState.class), same(eventSkipped));
        verify(fsm).onTestComplete(any(TestStartedState.class), any(TestCompletedState.class), same(eventStarted));
    }

    @Test
    public void testEventsDirectOrder() {
        TestSkipped eventSkipped = new TestSkipped();
        TestStarted eventStarted = new TestStarted();
        assertTrue("Result must be test started state", engine.fire(eventStarted) instanceof TestStartedState);
        assertTrue("Result must be test completed state", engine.fire(eventSkipped) instanceof TestCompletedState);
        verify(fsm).onTestStart(any(UndefinedState.class), any(TestStartedState.class), same(eventStarted));
        verify(fsm).onTestComplete(any(TestStartedState.class), any(TestCompletedState.class), same(eventSkipped));
    }
}
