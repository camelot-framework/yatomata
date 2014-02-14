package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.AfterTransit;
import ru.yandex.qatools.fsm.annotations.BeforeTransit;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static ru.yandex.qatools.fsm.annotations.Transitions.PREVIOUS;
import static ru.yandex.qatools.fsm.impl.Metadata.get;

/**
 * @author: Ilya Sadykov
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
        try {
            invokeTransitionHook(fsm, newState, event, BeforeTransit.class, false);
            invokeTransitionHook(fsm, newState, event, OnTransit.class, true);
            invokeTransitionHook(fsm, newState, event, AfterTransit.class, false);
        } catch (Exception e) {
            throw new StateMachineException("Could not invoke transition callback method from " + currentState + " to "
                    + newState + " on " + event + "!", e);
        }
        currentState = newState;
        completed = fsmClassInfo.isCompleted(fsm, newState, event, transit.stop());
        return newState;
    }

    private void invokeTransitionHook(T fsm, Object newState, Object event, Class annotationClass, boolean singleCall)
            throws InvocationTargetException, IllegalAccessException {
        Set<Method> called = new HashSet<>();
        for (Class oldStateClass : fsmClassInfo.getSuperClasses(currentState.getClass())) {
            for (Class newStateClass : fsmClassInfo.getSuperClasses(newState.getClass())) {
                for (Class eventClass : fsmClassInfo.getSuperClasses(event.getClass())) {
                    for (Method method : fsmClassInfo.getAnnotatedMethods(annotationClass)) {
                        if (method.getAnnotation(annotationClass) != null && !called.contains(method)) {
                            final Class<?>[] parameterTypes = method.getParameterTypes();
                            // Match the method and invoke if matches
                            if (parameterTypes.length == 3 && parameterTypes[0].equals(oldStateClass) &&
                                    parameterTypes[1].equals(newStateClass) && parameterTypes[2].equals(eventClass)) {
                                method.invoke(fsm, currentState, newState, event);
                                called.add(method);
                            } else if (parameterTypes.length == 2 && parameterTypes[0].equals(oldStateClass) && parameterTypes[1].equals(eventClass)) {
                                method.invoke(fsm, currentState, event);
                                called.add(method);
                            } else if (parameterTypes.length == 2 && parameterTypes[0].equals(newStateClass) && parameterTypes[1].equals(eventClass)) {
                                method.invoke(fsm, newState, event);
                                called.add(method);
                            } else if (parameterTypes.length == 1 && parameterTypes[0].equals(eventClass)) {
                                method.invoke(fsm, event);
                                called.add(method);
                            }
                        }
                        if (singleCall && called.size() > 0) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
