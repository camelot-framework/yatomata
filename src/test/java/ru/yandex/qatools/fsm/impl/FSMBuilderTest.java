package ru.yandex.qatools.fsm.impl;

import org.junit.Test;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.fsm.beans.TestEvent;
import ru.yandex.qatools.fsm.beans.TestStarted;
import ru.yandex.qatools.fsm.beans.TestStartedState;
import ru.yandex.qatools.fsm.beans.UndefinedState;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FSMBuilderTest {

    @FSM(start = UndefinedState.class)
    @Transitions({
            @Transit(from = UndefinedState.class, to = TestStartedState.class, on = TestEvent.class)
    })
    public static class FsmWithConstructor {
        
        private final AtomicInteger counter;
        
        FsmWithConstructor(int initialValue) {
            this.counter = new AtomicInteger(initialValue);
        }

        @OnTransit
        public void onTestStart(TestEvent event) {
            counter.getAndIncrement();
        }

        int getCounter() {
            return counter.get();
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void testFailedToInstantiateFsm() {
        new FSMBuilder<>(FsmWithConstructor.class).build();
    }
    
    @Test
    public void testFsmFromInstance() {
        FsmWithConstructor instance = new FsmWithConstructor(0);
        FSMBuilder<FsmWithConstructor> fsm = new FSMBuilder<>(instance);
        fsm.build().fire(new TestStarted());
        assertThat(instance.getCounter(), equalTo(1));
    }
    
}