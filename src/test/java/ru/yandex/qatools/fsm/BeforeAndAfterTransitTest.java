package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov
 */

public class BeforeAndAfterTransitTest {

    @FSM(start = UndefinedState.class)
    @Transitions({
            @Transit(from = UndefinedState.class, to = TestStartedState.class, on = TestStarted.class),
            @Transit(from = TestStartedState.class, to = TestCompletedState.class, on = TestEvent.class)
    })
    public interface BeforeAndAfterTransitStateMachine {
        @OnTransit
        public void onTestStart(UndefinedState oldState, TestStartedState newState, TestStarted event);

        @BeforeTransit
        public void beforeTransit(TestState state, TestEvent event);

        @BeforeTransit
        public void beforeTransit(TestState from, TestState to, TestEvent event);

        @BeforeTransit
        public void beforeTransit(TestEvent event);

        @AfterTransit
        public void afterTransit(TestEvent event);

        @AfterTransit
        public void afterTransit(TestState state, TestEvent event);

        @OnTransit
        public void onTestComplete(TestStartedState oldState, TestEvent event);
    }

    BeforeAndAfterTransitStateMachine fsm;
    Yatomata<BeforeAndAfterTransitStateMachine> engine;

    @Before
    public void init() throws FSMException {
        fsm = mock(BeforeAndAfterTransitStateMachine.class);
        engine = new YatomataImpl(BeforeAndAfterTransitStateMachine.class, fsm);
    }

    @Test
    public void testEvents() {
        TestStarted eventStarted = new TestStarted();
        TestSkipped eventSkipped = new TestSkipped();
        assertTrue("Result must be test started state", engine.fire(eventStarted) instanceof TestStartedState);
        assertTrue("Result must be test completed state", engine.fire(eventSkipped) instanceof TestCompletedState);
        InOrder inOrder = inOrder(fsm);
        inOrder.verify(fsm).beforeTransit(same(eventStarted));
        inOrder.verify(fsm).beforeTransit(any(TestStartedState.class), same(eventStarted));
        inOrder.verify(fsm).beforeTransit(any(UndefinedState.class), any(TestStartedState.class), same(eventStarted));
        inOrder.verify(fsm).onTestStart(any(UndefinedState.class), any(TestStartedState.class), same(eventStarted));
        inOrder.verify(fsm).afterTransit(same(eventStarted));
        inOrder.verify(fsm).afterTransit(any(TestStartedState.class), same(eventStarted));

        inOrder.verify(fsm).beforeTransit(same(eventSkipped));
        inOrder.verify(fsm).beforeTransit(any(TestCompletedState.class), same(eventSkipped));
        inOrder.verify(fsm).beforeTransit(any(TestStartedState.class), any(TestCompletedState.class), same(eventSkipped));
        inOrder.verify(fsm).onTestComplete(any(TestStartedState.class), same(eventSkipped));
        inOrder.verify(fsm).afterTransit(same(eventSkipped));
        inOrder.verify(fsm).afterTransit(any(TestCompletedState.class), same(eventSkipped));
    }
}
