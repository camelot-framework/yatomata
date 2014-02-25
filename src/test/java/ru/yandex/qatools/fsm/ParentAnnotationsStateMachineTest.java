package ru.yandex.qatools.fsm;

import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.CompletedCounterState;
import ru.yandex.qatools.fsm.beans.TestCompletionInfo;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author: Ilya Sadykov
 */
public class ParentAnnotationsStateMachineTest {

    @FSM(start = CompletedCounterState.class)
    @Transitions(
            @Transit(on = {TestCompletionInfo.class})
    )
    public static class CompletedCounterStateMachine {
        @OnTransit
        public void aggregate(CompletedCounterState state, TestCompletionInfo event) {
        }
    }

    public static class CompletedCounterStateMachineExtended extends CompletedCounterStateMachine {
    }

    @Test
    public void testAggregateIsCalled() throws FSMException {
        CompletedCounterStateMachineExtended fsm = mock(CompletedCounterStateMachineExtended.class);
        final CompletedCounterState state = new CompletedCounterState();
        Yatomata<CompletedCounterStateMachineExtended> engine = new YatomataImpl<>(CompletedCounterStateMachineExtended.class, fsm, state);

        final TestCompletionInfo event = new TestCompletionInfo();
        engine.fire(event);
        verify(fsm).aggregate(same(state), same(event));
    }
}
