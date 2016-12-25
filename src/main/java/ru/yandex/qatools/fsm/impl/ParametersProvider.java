package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.FSMException;
import ru.yandex.qatools.fsm.annotations.Event;
import ru.yandex.qatools.fsm.annotations.FromState;
import ru.yandex.qatools.fsm.annotations.OnException;
import ru.yandex.qatools.fsm.annotations.ToState;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ParametersProvider {

    private final Throwable exception;

    private final Object fromState;

    private final Object toState;

    private final Object event;

    ParametersProvider(Throwable exception, Object fromState, Object toState, Object event) {
        this.exception = exception;
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
    }

    ParametersProvider(Object fromState, Object toState, Object event) {
        this(null, fromState, toState, event);
    }

    List<Object> provide() {
        List<Object> ret = new ArrayList<>();
        for (Object element : Arrays.asList(exception, fromState, toState, event)) {
            if (element != null) {
                ret.add(element);
            }
        }
        return ret;
    }

    List<Object> provide(Method method) throws FSMException {
        List<Object> ret = new ArrayList<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] allParametersAnnotations = method.getParameterAnnotations();
        for (int i = 0; i <= allParametersAnnotations.length - 1; i++) {
            Annotation[] parameterAnnotations = allParametersAnnotations[i];
            Class<?> parameterType = parameterTypes[i];
            List<Class<? extends Annotation>> suitableAnnotations = getSuitableAnnotations(parameterAnnotations);
            
            //No need to annotate exception
            boolean isExceptionParameter = Throwable.class.isAssignableFrom(parameterType);
            if (suitableAnnotations.isEmpty() && !isExceptionParameter) {
                return Collections.emptyList();
            }
            if (suitableAnnotations.size() > 1) {
                throw new FSMException(String.format(
                        "Invalid FSM method %s: only one of @Event, @FromState, @ToState can be used on each parameter.",
                        method.getName()
                ));
            }
            Class<? extends Annotation> annClass = isExceptionParameter ?
                    OnException.class : suitableAnnotations.get(0);
            Object value = getValue(annClass);
            if (value != null && !parameterType.isAssignableFrom(value.getClass())) {
                return Collections.emptyList();
            }
            ret.add(value);
        }
        return ret;
    }
    
    private static List<Class<? extends Annotation>> getSuitableAnnotations(Annotation[] parameterAnnotations) {
        List<Class<? extends Annotation>> suitableAnnotations = new ArrayList<>();
        for (Annotation ann : parameterAnnotations) {
            if (isSuitableAnnotation(ann.getClass())) {
                suitableAnnotations.add(ann.getClass());
            }
        }
        return suitableAnnotations;
    }

    private Object getValue(Class<? extends Annotation> annClass) {
        if (isFromState(annClass)) {
            return fromState;
        } else if (isToState(annClass)) {
            return toState;
        } else if (isEvent(annClass)) {
            return event;
        } else if (OnException.class.equals(annClass)) {
            return exception;
        }
        throw new IllegalArgumentException("Unsupported annotation: @" + annClass.getCanonicalName());
    }

    private static boolean isSuitableAnnotation(Class<? extends Annotation> annClass) {
        return isFromState(annClass) || isToState(annClass) || isEvent(annClass);
    }

    private static boolean isFromState(Class<? extends Annotation> annClass) {
        return FromState.class.isAssignableFrom(annClass);
    }

    private static boolean isToState(Class<? extends Annotation> annClass) {
        return ToState.class.isAssignableFrom(annClass);
    }

    private static boolean isEvent(Class<? extends Annotation> annClass) {
        return Event.class.isAssignableFrom(annClass);
    }

}
