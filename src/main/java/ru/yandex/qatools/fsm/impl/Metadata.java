package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.StateMachineException;
import ru.yandex.qatools.fsm.StopConditionAware;
import ru.yandex.qatools.fsm.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.*;

/**
 * @author: Ilya Sadykov
 */
class Metadata {

    private static final Map<Class<?>, ClassInfo> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] METHOD_ANNOTATIONS = new Class[]{
            OnTransit.class, BeforeTransit.class, AfterTransit.class
    };

    public static ClassInfo get(Class fsmClass) throws FSMException {
        if (!cache.containsKey(fsmClass)) {
            cache.put(fsmClass, new ClassInfo(fsmClass));
        }
        return cache.get(fsmClass);
    }

    public static class ClassInfo<T> {
        private final Class<T> fsmClass;
        private final FSM fsmConfig;
        private final Transitions transitions;
        private final Map<Class<? extends Annotation>, Method[]> annotatedMethods;
        private final Map<Class, Class[]> superClassesCache;
        private final boolean stoppedByCondition;

        private ClassInfo(Class<T> fsmClass) {
            this.fsmClass = fsmClass;
            this.fsmConfig = fsmClass.getAnnotation(FSM.class);
            if (fsmConfig == null) {
                throw new StateMachineException("FSM class must have the @FSM annotation!");
            }
            transitions = fsmClass.getAnnotation(Transitions.class);
            annotatedMethods = buildMethodsCache();
            superClassesCache = buildStateSuperClassesCache();
            stoppedByCondition = StopConditionAware.class.isAssignableFrom(fsmClass);
        }

        public Object initNewState(Object fsm, Class newStateClass, Object event) {
            try {
                final String initStateMethod = fsmConfig.initStateMethod();
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

        public Transitions getTransitions() {
            return transitions;
        }

        public Method[] getAnnotatedMethods(Class aClass) {
            return annotatedMethods.get(aClass);
        }

        public Object initStartState(Object fsm) {
            return initNewState(fsm, fsmConfig.start(), null);
        }

        public boolean isCompleted(T fsm, Object newState, Object event, boolean stoppedByTransition) {
            if (stoppedByCondition) {
                return ((StopConditionAware) fsm).isStopRequired(newState, event) || stoppedByTransition;
            }
            return stoppedByTransition;
        }

        public Class[] getSuperClasses(Class clazz) {
            if (superClassesCache.containsKey(clazz)) {
                return superClassesCache.get(clazz);
            }
            final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
            final Class[] classesArray = new Class[classes.size()];
            addCollectedSuperclasses(superClassesCache, classesArray);
            return classes.toArray(classesArray);
        }

        public Transit findSingleTransition(Object currentState, Object event) {
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

        public List<Transit> findTransitions(Class stateClass, Class eventClass) {
            List<Transit> transits = new ArrayList<>();
            for (Transit transit : transitions.value()) {
                if (containsClass(transit.from(), Transitions.ANY.class) || containsClass(transit.from(), stateClass)) {
                    if (containsClass(transit.on(), eventClass)) {
                        transits.add(transit);
                    }
                }
            }
            return transits;
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

    }
}
