package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.FSMException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class BestMatchedAnnotatedMethodCaller {

    private final Object instance;
    private final Metadata.ClassInfo cache;

    public BestMatchedAnnotatedMethodCaller(Object instance, Metadata.ClassInfo cache) {
        this.cache = cache;
        this.instance = instance;
    }

    public Collection<Method> call(Class<? extends Annotation> annClass, boolean singleCall, ParametersProvider parametersProvider) throws Throwable {
        try {
            final HashSet<Method> called = new HashSet<>();
            callMethodsWithAnnotatedParameters(annClass, parametersProvider, called);
            call(annClass, parametersProvider.provide(), singleCall, called);
            return called;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw (e.getCause() != null) ? e.getCause() : e;
        }
    }

    private void callMethodsWithAnnotatedParameters(Class<? extends Annotation> annClass, ParametersProvider parametersProvider, Set<Method> called) throws Throwable {
        for (Method method : cache.getAnnotatedMethods(annClass)) {
            List<Object> params = parametersProvider.provide(method);
            if (!params.isEmpty()) {
                callMethod(method, params, called);
            }
        }
    }

    private void call(Class<? extends Annotation> annClass, List<Object> params, boolean singleCall, Set<Method> called) throws Throwable {
        final List<Class<?>> paramTypes = new ArrayList<>();
        final List<Object> paramList = new ArrayList<>();
        for (Object value : params) {
            paramTypes.add(value.getClass());
            paramList.add(value);
        }
        call(annClass, paramTypes, paramList, new Stack<Class<?>>(), singleCall, called);
    }

    private void call(Class<? extends Annotation> annClass,
                      List<Class<?>> paramTypes,
                      List<Object> params, Stack<Class<?>> typesStack, boolean singleCall, Set<Method> called) throws Throwable {
        if (!called.isEmpty() && singleCall) {
            return;
        }
        if (params.isEmpty()) {
            throw new FSMException("Failed to invoke methods annotated with @" + annClass + ": parameters are empty!");
        }
        if (typesStack.size() >= paramTypes.size()) {
            findSuitableMethodAndCall(annClass, paramTypes, params, typesStack, singleCall, called);
        } else {
            final int pIdx = typesStack.size() > 0 ? typesStack.size() : 0;
            for (Class clazz : cache.getSuperClasses(paramTypes.get(pIdx))) {
                typesStack.push(clazz);
                findSuitableMethodAndCall(annClass, paramTypes, params, typesStack, singleCall, called);
                typesStack.pop();
                if (!called.isEmpty() && singleCall) {
                    return;
                }
            }
        }
    }

    private void findSuitableMethodAndCall(Class<? extends Annotation> annClass, List<Class<?>> paramTypes,
                                           List<Object> params, Stack<Class<?>> types,
                                           boolean singleCall, Set<Method> called) throws Throwable {
        for (Method method : cache.getAnnotatedMethods(annClass)) {
            final List<Class> mParamTypes = Arrays.<Class>asList(method.getParameterTypes());
            // trying to call the full-arguments method
            if (checkMethodParams(mParamTypes, types)) {
                callMethod(method, types, params.subList(0, types.size()), called);
                if (!called.isEmpty() && singleCall) {
                    return;
                }
            }
            // trying to apply the partial call if method has less arguments, i.e. (A, B, C, D) -> (A, B, C) || (A, B) || (B, C, D) || (B, C) || (C, D) || (B) || (C) || (D)
            if (mParamTypes.size() < types.size()) {
                for (int j = types.size(); j > 0; --j) {
                    final List<Class<?>> partTypes = types.subList(0, j);
                    for (int i = 1; i < partTypes.size(); ++i) {
                        final List<Class<?>> typesSublist = partTypes.subList(i, partTypes.size());
                        if (mParamTypes.size() == typesSublist.size() && checkMethodParams(mParamTypes, typesSublist)) {
                            callMethod(method, typesSublist, params.subList(i, i + typesSublist.size()), called);
                            if (!called.isEmpty() && singleCall) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        // if there are still some arguments to be checked
        if (types.size() < params.size()) {
            call(annClass, paramTypes, params, types, singleCall, called);
        }
    }

    private void callMethod(Method method, List<Class<?>> types, List<Object> params, Set<Method> called) throws IllegalAccessException, InvocationTargetException {
        if (types.size() == method.getParameterTypes().length) {
            callMethod(method, params, called);
        }
    }

    private void callMethod(Method method, List<Object> params, Set<Method> called) throws IllegalAccessException, InvocationTargetException {
        if (!called.contains(method)) {
            method.invoke(instance, params.toArray(new Object[method.getParameterTypes().length]));
            called.add(method);
        }
    }


    private boolean checkMethodParams(List<Class> paramTypes, List<Class<?>> types) {
        if (types.size() > paramTypes.size()) {
            return false;
        }
        for (int i = 0; i < types.size(); ++i) {
            if (!paramTypes.get(i).equals(types.get(i))) {
                return false;
            }
        }
        return true;
    }
}
