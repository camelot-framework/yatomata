package ru.yandex.qatools.fsm.impl;

import org.junit.Test;
import org.mockito.InOrder;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnException;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static org.mockito.Mockito.*;
import static ru.yandex.qatools.fsm.impl.Metadata.get;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BestMatchedAnnotatedMethodCallerTest {

    class BaseState {

    }

    class DerivedState extends BaseState {
    }

    class ConcreteState extends BaseState {
    }

    interface Event {

    }

    class BaseEvent implements Event {

    }

    class DerivedEvent extends BaseEvent {

    }

    class ConcreteEvent extends DerivedEvent {

    }

    @FSM(start = Object.class)
    @Transitions(@Transit(on = String.class))
    public static class SomeClass {
        @OnException
        public void mBaseStateDerivedEventObject(BaseState param1, DerivedEvent param2, Object param3) {

        }

        @OnException
        public void mBaseStateBaseEventObject(BaseState param1, BaseEvent param2, Object param3) {

        }

        @OnException
        public void mBaseStateConcreteEvent(BaseState param1, ConcreteEvent param2) {

        }

        @OnException
        public void mConcreteStateConcreteEvent(ConcreteState param1, ConcreteEvent param2) {

        }

        @OnException
        public void mConcreteEventObject(ConcreteEvent param2, Object param3) {

        }

        @OnException
        public void mBaseStateEvent(BaseState state, Event param2) {

        }
    }

    @Test
    public void testBoth() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        BestMatchedAnnotatedMethodCaller caller = new BestMatchedAnnotatedMethodCaller(obj, get(SomeClass.class));
        BaseState state = mock(BaseState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);
        Object param = mock(Object.class);

        caller.call(OnException.class, false, state, event, param);
        InOrder inOrder = inOrder(obj);
        inOrder.verify(obj).mConcreteEventObject(event, param);
        inOrder.verify(obj).mBaseStateConcreteEvent(state, event);
        inOrder.verify(obj).mBaseStateDerivedEventObject(state, event, param);
        inOrder.verify(obj).mBaseStateBaseEventObject(state, event, param);
        inOrder.verify(obj).mBaseStateEvent(state, event);
        verifyNoMoreInteractions(obj);
    }

    @Test
    public void testBase() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        BestMatchedAnnotatedMethodCaller caller = new BestMatchedAnnotatedMethodCaller(obj, get(SomeClass.class));
        DerivedState state = mock(DerivedState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);
        Object param = mock(Object.class);

        caller.call(OnException.class, true, state, event, param);
        verify(obj).mConcreteEventObject(event, param);
        verifyNoMoreInteractions(obj);
    }

    @Test
    public void testDerived() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        BestMatchedAnnotatedMethodCaller caller = new BestMatchedAnnotatedMethodCaller(obj, get(SomeClass.class));
        DerivedState state = mock(DerivedState.class);
        BaseEvent event = mock(BaseEvent.class);
        Object param = mock(Object.class);

        caller.call(OnException.class, true, state, event, param);
        verify(obj).mBaseStateBaseEventObject(state, event, param);
        verifyNoMoreInteractions(obj);
    }

    @Test
    public void testConcrete() throws Throwable {
        SomeClass obj = mock(SomeClass.class);
        BestMatchedAnnotatedMethodCaller caller = new BestMatchedAnnotatedMethodCaller(obj, get(SomeClass.class));
        ConcreteState state = mock(ConcreteState.class);
        ConcreteEvent event = mock(ConcreteEvent.class);

        caller.call(OnException.class, true, state, event);
        verify(obj).mConcreteStateConcreteEvent(state, event);
        verifyNoMoreInteractions(obj);
    }
}
