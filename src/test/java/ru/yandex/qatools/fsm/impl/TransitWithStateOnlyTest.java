package ru.yandex.qatools.fsm.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov
 */

public class TransitWithStateOnlyTest {

    @FSM(start = UndefinedState.class)
    @Transitions({
            @Transit(from = {TestStartedState.class, UndefinedState.class}, to = TestStartedState.class, on = TestStarted.class),
    })
    public interface TransitWithStateOnlyArgumentFSM {

        @OnTransit
        public void onTestStarted(UndefinedState state);

        @OnTransit
        public void onTestStarted(TestStartedState state);
    }

    TransitWithStateOnlyArgumentFSM fsm;
    Yatomata<TransitWithStateOnlyArgumentFSM> engine;

    @Before
    public void init() throws FSMException {
        fsm = mock(TransitWithStateOnlyArgumentFSM.class);
        engine = new YatomataImpl<>(TransitWithStateOnlyArgumentFSM.class, fsm);
    }

    @Test
    public void testEvents() {
        TestStarted event = new TestStarted();
        engine.fire(event);
        engine.fire(event);
        InOrder inOrder = inOrder(fsm);
        inOrder.verify(fsm).onTestStarted(any(UndefinedState.class));
        inOrder.verify(fsm).onTestStarted(any(TestStartedState.class));
        verifyNoMoreInteractions(fsm);
    }
}
