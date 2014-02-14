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
 * @author: Ilya Sadykov
 */

public class FallenRaisedStateMachineTest {

    @FSM(start = UndefinedState.class)
    @Transitions({
            @Transit(to = TestPassedState.class, on = TestPassed.class),
            @Transit(to = TestFailedState.class, on = TestFailed.class),
            @Transit(to = TestSkippedState.class, on = TestSkipped.class),
            @Transit(to = TestBrokenState.class, on = TestBroken.class),
            @Transit(to = TestDroppedState.class, on = TestDropped.class, stop = true)
    })
    public interface FallenRaisedStateMachine {
        @OnTransit
        public void onTestPassed(TestState oldState, TestState newState, TestPassed event);

        @OnTransit
        public void onTestBroken(TestState currentState, TestBroken event);

        @OnTransit
        public void onTestSkipped(TestState currentState, TestSkipped event);

        @OnTransit
        public void onTestDropped(TestState state, TestDropped event);
    }

    FallenRaisedStateMachine fsm;
    Yatomata<FallenRaisedStateMachine> engine;

    @Before
    public void init() {
        fsm = mock(FallenRaisedStateMachine.class);
        engine = new YatomataImpl(FallenRaisedStateMachine.class, fsm);
    }

    @Test
    public void testTestBrokenAfterSkipped() {
        TestSkipped eventSkipped = new TestSkipped();
        TestState state = (TestState) engine.fire(eventSkipped);
        assertTrue("Result must be test skipped state", state instanceof TestSkippedState);
        verify(fsm).onTestSkipped(any(UndefinedState.class), same(eventSkipped));

        TestBroken eventBroken = new TestBroken();
        state = (TestState) engine.fire(eventBroken);
        assertTrue("Result must be test broken state", state instanceof TestBrokenState);
        verify(fsm).onTestBroken(any(TestSkippedState.class), same(eventBroken));
    }

    @Test
    public void testTestBroken() {
        TestBroken event = new TestBroken();
        TestState state = (TestState) engine.fire(event);
        assertTrue("Result must be test broken state", state instanceof TestBrokenState);
        verify(fsm).onTestBroken(any(UndefinedState.class), same(event));
    }

    @Test
    public void testTestPassed() {
        TestPassed event = new TestPassed();
        TestState state = (TestState) engine.fire(event);
        assertTrue("Result must be test passed state", state instanceof TestPassedState);
        verify(fsm).onTestPassed(any(UndefinedState.class), any(TestPassedState.class), same(event));
    }

    @Test
    public void testDropped() {
        TestDropped event = new TestDropped();
        TestState state = (TestState) engine.fire(event);
        assertTrue("Result must be test dropped", state instanceof TestDroppedState);
        verify(fsm).onTestDropped(any(UndefinedState.class), same(event));
        assertTrue("FSM must be stopped", engine.isCompleted());
    }
}
