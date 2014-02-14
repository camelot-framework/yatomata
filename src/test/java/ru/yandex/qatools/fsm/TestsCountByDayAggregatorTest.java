package ru.yandex.qatools.fsm;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.*;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author: Ilya Sadykov
 */

public class TestsCountByDayAggregatorTest {

    @FSM(start = TestsCountByDayState.class)
    @Transitions(
            @Transit(on = {TestPassed.class, TestSkipped.class, TestBroken.class, TestFailed.class})
    )
    public interface TestsCountByDayAggregator {

        @OnTransit
        public void count(TestsCountByDayState state, TestSkipped event);

        @OnTransit
        public void count(TestsCountByDayState state, TestEvent event);
    }

    TestsCountByDayAggregator fsm;
    Yatomata<TestsCountByDayAggregator> engine;

    @Before
    public void init() throws FSMException {
        fsm = mock(TestsCountByDayAggregator.class);
        engine = new YatomataImpl(TestsCountByDayAggregator.class, fsm);
    }

    @Test
    public void testEventsReverse() {
        TestSkipped eventSkipped = new TestSkipped();
        TestBroken eventBroken = new TestBroken();
        engine.fire(eventSkipped);
        engine.fire(eventBroken);
        verify(fsm).count(any(TestsCountByDayState.class), same(eventSkipped));
        verify(fsm).count(any(TestsCountByDayState.class), same(eventBroken));
    }
}
