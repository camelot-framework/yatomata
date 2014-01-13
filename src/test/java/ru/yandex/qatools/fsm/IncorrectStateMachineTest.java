package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class IncorrectStateMachineTest {

    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = ProcessStarted.class, to = Running.class),
            @Transit(from = Running.class, on = ProcessCompleted.class, to = Idle.class),
            @Transit(from = Running.class, on = ProcessCompleted.class, to = Cancelling.class),
            @Transit(from = Running.class, on = ProcessFailed.class, to = NoninstantiatableState.class),
            @Transit(from = Running.class, on = ProcessCancelled.class, to = Cancelling.class),
    })
    public class IncorrectStateMachine {
        @OnTransit
        public void onCancel(Running from, Cancelling to, ProcessCancelled event) {
            throw new RuntimeException("Failure");
        }
    }

    public interface NoninstantiatableState {

    }

    private YatomataImpl engine;

    @Before
    public void init() {
        engine = new YatomataImpl(IncorrectStateMachine.class, new IncorrectStateMachine());
    }

    @Test(expected = StateMachineException.class)
    public void testIncorrectState() {
        engine.fire(new ProcessStarted());
        engine.fire(new ProcessFailed());
    }

    @Test(expected = StateMachineException.class)
    public void testIncorrectTransition() {
        engine.fire(new ProcessStarted());
        engine.fire(new ProcessCompleted());
    }

    @Test(expected = StateMachineException.class)
    public void testIncorrectHook() {
        engine.fire(new ProcessStarted());
        engine.fire(new ProcessCancelled());
    }

}
