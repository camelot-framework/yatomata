package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.fsm.annotations.Transitions.ANY;
import static ru.yandex.qatools.fsm.annotations.Transitions.PREVIOUS;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.*;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class YatomataImpl<T> implements Yatomata<T> {
    private final Class<T> fsmClass;
    private Object currentState;
    private boolean completed = false;
    private T fsm;
    private final FSM fsmConfig;
    private final Transitions transitions;
    private final Map<Class<? extends Annotation>, Method[]> annotatedMethods;
    private final Map<Class, Class[]> superClassesCache;

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] METHOD_ANNOTATIONS = new Class[]{
            OnTransit.class, BeforeTransit.class, AfterTransit.class
    };

    /**
     * Constructs the engine with the default state and initialize the new FSM instance
     */
    public YatomataImpl(Class<T> fsmClass) throws IllegalAccessException, InstantiationException {
        this(fsmClass, fsmClass.newInstance());
    }

    /**
     * Constructs the engine with the default state and the defined FSM instance
     */
    public YatomataImpl(Class<T> fsmClass, T fsm) {
        this.fsmClass = fsmClass;
        this.fsm = fsm;
        this.fsmConfig = fsmClass.getAnnotation(FSM.class);
        if (fsmConfig == null) {
            throw new StateMachineException("FSM class must have the @FSM annotation!");
        }
        this.currentState = initStartState();
        transitions = fsmClass.getAnnotation(Transitions.class);
        annotatedMethods = buildMethodsCache();
        superClassesCache = buildStateSuperClassesCache();
    }

    /**
     * Constructs the engine with the defined state
     */
    public YatomataImpl(Class<T> fsmClass, T fsm, Object currentState) {
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
        Transit transit = findSingleTransition(event);

        // if the transition is not found, ignoring the event
        if (transit == null) {
            return currentState;
        }
        Object newState = currentState;
        completed = transit.stop();

        // if transition to is not to previous or to the same as before state
        if (!transit.to().equals(PREVIOUS.class) && !transit.to().equals(currentState.getClass())) {
            newState = initNewState(transit.to(), event);
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
        return newState;
    }

    private Object initNewState(Class newStateClass, Object event) {
        try {
            final String initStateMethod = getFSMConfig().initStateMethod();
            if (!isEmpty(initStateMethod)) {
                for (Method m : getMethodsInClassHierarchy(fsmClass)) {
                    if (m.getName().equals(initStateMethod)) {
                        return m.invoke(fsm, newStateClass, event);
                    }
                }
                throw new StateMachineException("Could not find the suitable init state method with name '" +
                        initStateMethod + "' within the FSM class!");
            } else {
                return newStateClass.newInstance();
            }
        } catch (Exception e) {
            throw new StateMachineException("Could not instantiate new state!", e);
        }
    }

    private Object initStartState() {
        return initNewState(getFSMConfig().start(), null);
    }

    private FSM getFSMConfig() {
        return fsmConfig;
    }

    private Map<Class, Class[]> buildStateSuperClassesCache() {
        Map<Class, Class[]> superclasses = new HashMap<>();
        addCollectedSuperclasses(superclasses, fsmConfig.start());
        for (Transit transit : transitions.value()) {
            addCollectedSuperclasses(superclasses, transit.from());
            addCollectedSuperclasses(superclasses, transit.to());
        }
        return superclasses;
    }

    private void addCollectedSuperclasses(Map<Class, Class[]> superclasses, Class... stateClass) {
        for (Class clazz : stateClass) {
            if (!superclasses.containsKey(clazz)) {
                final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
                final Class[] classesArray = classes.toArray(new Class[classes.size()]);
                superclasses.put(clazz, classesArray);
                addCollectedSuperclasses(superclasses, classesArray);
            }
        }
    }

    private Map<Class<? extends Annotation>, Method[]> buildMethodsCache() {
        Map<Class<? extends Annotation>, Method[]> annotatedMethods = new HashMap<>();
        for (Class<? extends Annotation> annClass : METHOD_ANNOTATIONS) {
            List<Method> methods = new ArrayList<>();
            for (Method method : getMethodsInClassHierarchy(fsmClass)) {
                if (method.getAnnotation(annClass) != null) {
                    methods.add(method);
                }
            }
            annotatedMethods.put(annClass, methods.toArray(new Method[methods.size()]));
        }
        return annotatedMethods;
    }

    private Class[] getSuperClasses(Class clazz) {
        if (superClassesCache.containsKey(clazz)) {
            return superClassesCache.get(clazz);
        }
        final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
        final Class[] classesArray = new Class[classes.size()];
        addCollectedSuperclasses(superClassesCache, classesArray);
        return classes.toArray(classesArray);
    }

    private void invokeTransitionHook(T fsm, Object newState, Object event, Class annotationClass, boolean singleCall)
            throws InvocationTargetException, IllegalAccessException {
        Set<Method> called = new HashSet<>();
        for (Class oldStateClass : getSuperClasses(currentState.getClass())) {
            for (Class newStateClass : getSuperClasses(newState.getClass())) {
                for (Class eventClass : getSuperClasses(event.getClass())) {
                    for (Method method : annotatedMethods.get(annotationClass)) {
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

    private Transit findSingleTransition(Object event) {
        for (Class stateClass : getSuperClasses(currentState.getClass())) {
            for (Class eventClass : getSuperClasses(event.getClass())) {
                List<Transit> transits = findTransitions(stateClass, eventClass);
                if (transits.size() > 1) {
                    throw new StateMachineException("There's more than 1 transition found!");
                }
                if (!transits.isEmpty()) {
                    return transits.get(0);
                }
            }
        }
        return null;
    }

    private List<Transit> findTransitions(Class stateClass, Class eventClass) {
        List<Transit> transits = new ArrayList<>();
        for (Transit transit : transitions.value()) {
            if (containsClass(transit.from(), ANY.class) || containsClass(transit.from(), stateClass)) {
                if (containsClass(transit.on(), eventClass)) {
                    transits.add(transit);
                }
            }
        }
        return transits;
    }
}
