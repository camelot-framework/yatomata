package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.*;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.lang.String.format;
import static ru.yandex.qatools.fsm.annotations.Transitions.PREVIOUS;
import static ru.yandex.qatools.fsm.impl.Metadata.get;

/**
 * @author Ilya Sadykov
 */
public class YatomataImpl<T> implements Yatomata<T> {
    private final Class<T> fsmClass;
    private Object currentState;
    private boolean completed = false;
    private T fsm;
    private final Metadata.ClassInfo fsmClassInfo;

    /**
     * Constructs the engine with the default state and initialize the new FSM instance
     */
    public YatomataImpl(Class<T> fsmClass) throws FSMException, IllegalAccessException, InstantiationException {
        this(fsmClass, fsmClass.newInstance());
    }

    /**
     * Constructs the engine with the default state and the defined FSM instance
     */
    public YatomataImpl(Class<T> fsmClass, T fsm) throws FSMException {
        this.fsmClass = fsmClass;
        this.fsm = fsm;
        this.fsmClassInfo = get(fsmClass);
        this.currentState = fsmClassInfo.initStartState(fsm);
    }

    /**
     * Constructs the engine with the defined state
     */
    public YatomataImpl(Class<T> fsmClass, T fsm, Object currentState) throws FSMException {
        this(fsmClass, fsm);
        this.currentState = currentState;
    }

    /**
     * Returns the current state for the FSM
     */
    @Override
    public Object getCurrentState() {
        return currentState;
    }

    /**
     * Checks if the FSM is already completed
     */
    @Override
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the internal FSM class
     */
    @Override
    public Class<T> getFSMClass() {
        return fsmClass;
    }

    /**
     * Returns the internal FSM instance
     */
    @Override
    public T getFSM() {
        return fsm;
    }

    /**
     * Fires new event into the state machine
     */
    @Override
    public Object fire(Object event) {

        if (completed) {
            throw new StateMachineException("State machine is already completed!");
        }
        if (event == null) {
            return currentState;
        }
        if (currentState == null) {
            throw new StateMachineException("Current state cannot be null!");
        }

        // search for the single available transition
        Transit transit = fsmClassInfo.findSingleTransition(currentState, event);

        // if the transition is not found, ignoring the event
        if (transit == null) {
            return currentState;
        }
        Object newState = currentState;

        // if transition to is not to previous or to the same as before state
        if (!transit.to().equals(PREVIOUS.class) && !transit.to().equals(currentState.getClass())) {
            newState = fsmClassInfo.initNewState(fsm, transit.to(), event);
        }
        final BestMatchedAnnotatedMethodCaller caller = new BestMatchedAnnotatedMethodCaller(fsm, fsmClassInfo);
        boolean completeTransition = false;
        try {
            caller.call(BeforeTransit.class, false, currentState, newState, event);
            caller.call(OnTransit.class, true, currentState, newState, event);
            caller.call(AfterTransit.class, false, currentState, newState, event);
            completeTransition = true;
        } catch (Throwable e) {
            try {
                Collection<Method> called;
                for (Method m : called = caller.call(OnException.class, true, e, currentState, newState, event)) {
                    completeTransition = completeTransition || (m.getAnnotation(OnException.class).preserve());
                }
                if (called.isEmpty()) {
                    throw new StateMachineException(format("Could not invoke transition callback method " +
                            "with FSM %s  (%s) -> (%s) on %s!", fsm, currentState, newState, event), e);
                }
            } catch (Throwable onE) {
                throw new StateMachineException(format("Could not invoke the @OnException method for FSM %s " +
                        "while trying to transit (%s) -> (%s) on %s!", fsm, currentState, newState, event), e);
            }
        }
        if (completeTransition) {
            currentState = newState;
        }
        completed = fsmClassInfo.isCompleted(fsm, newState, event, transit.stop());
        return currentState;
    }
}
