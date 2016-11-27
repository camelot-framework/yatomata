package ru.yandex.qatools.fsm.impl;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.TestEvent;
import ru.yandex.qatools.fsm.beans.TestStarted;
import ru.yandex.qatools.fsm.beans.UndefinedEvent;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov
 */

public class TransitWithSameStateAndEventClassTest {

    @FSM(start = UndefinedEvent.class)
    @Transitions({
            @Transit(from = UndefinedEvent.class, to = TestStarted.class, on = TestStarted.class),
    })
    public class TransitWithSameStateAndEventClassStateMachine {
        @NewState
        public TestEvent newState(Class<? extends TestEvent> clazz, TestEvent event) {
            return event;
        }

        @OnTransit
        public void onTestStarted(TestStarted event) {
            System.out.println(event);
        }
    }

    TransitWithSameStateAndEventClassStateMachine fsm;
    Yatomata<TransitWithSameStateAndEventClassStateMachine> engine;

    @Before
    public void init() throws FSMException {
        fsm = mock(TransitWithSameStateAndEventClassStateMachine.class, CALLS_REAL_METHODS);
        engine = new YatomataImpl<>(TransitWithSameStateAndEventClassStateMachine.class, fsm);
    }

    @Test
    public void testEvents() {
        TestStarted event = new TestStarted();
        engine.fire(event);
        verify(fsm).newState(same(TestStarted.class), same(event));
        verify(fsm).onTestStarted(same(event));
        verifyNoMoreInteractions(fsm);
    }
}
