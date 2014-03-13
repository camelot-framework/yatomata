package ru.yandex.qatools.fsm;

import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.CompletedCounterState;
import ru.yandex.qatools.fsm.beans.TestCompletionInfo;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Ilya Sadykov
 */
public class CompletedCounterStateMachineTest {

    @FSM(start = CompletedCounterState.class)
    @Transitions(
            @Transit(on = {TestCompletionInfo.class})
    )
    public static interface CompletedCounterStateMachine {
        @OnTransit
        public void aggregate(CompletedCounterState state, TestCompletionInfo event);
    }

    public static class CompletedCounterStateMachineImpl implements CompletedCounterStateMachine {

        @Override
        public void aggregate(CompletedCounterState state, TestCompletionInfo event) {
            state.setCompletionsCount(state.getCompletionsCount() + 1);
        }
    }

    @Test
    public void testAggregateState() throws FSMException {
        final CompletedCounterStateMachineImpl fsm = new CompletedCounterStateMachineImpl();
        final CompletedCounterState state = new CompletedCounterState();
        Yatomata<CompletedCounterStateMachine> engine = new YatomataImpl<>(CompletedCounterStateMachine.class, fsm, state);
        for (int i = 0; i < 10; ++i) {
            engine.fire(new TestCompletionInfo());
        }
        assertEquals("Completions count must be 10", state.getCompletionsCount(), 10);
    }

    @Test
    public void testAggregateIsCalled() throws FSMException {
        CompletedCounterStateMachine fsm = mock(CompletedCounterStateMachine.class);
        final CompletedCounterState state = new CompletedCounterState();
        Yatomata<CompletedCounterStateMachine> engine = new YatomataImpl<>(CompletedCounterStateMachine.class, fsm, state);

        final TestCompletionInfo event = new TestCompletionInfo();
        engine.fire(event);
        verify(fsm).aggregate(same(state), same(event));
    }
}
