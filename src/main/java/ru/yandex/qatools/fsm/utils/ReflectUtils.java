package ru.yandex.qatools.fsm.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ilya Sadykov
 */
public class ReflectUtils {

    /**
     * Collect all superclasses and their interfaces of a class in the defined order:
     * Class -> its interfaces -> superclass -> its interfaces -> superclass of a superclass -> ...
     */
    public static List<Class> collectAllSuperclassesAndInterfaces(final Class objClazz) {
        List<Class> result = new ArrayList<Class>();
        Class clazz = objClazz;
        // search through superclasses
        while (clazz != null) {
            result.add(clazz);
            for(Class iface : clazz.getInterfaces()){
                result.addAll(collectAllSuperclassesAndInterfaces(iface));
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    /**
     * Checks if the list of the classes contain the class
     */
    public static boolean containsClass(Class[] list, Class clazz) {
        for (Class c : list) {
            if (c.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for all methods within class hierarchy
     */
    public static Method[] getMethodsInClassHierarchy(Class<?> clazz) {
        Method[] methods = {};
        while (clazz != null) {
            methods = ArrayUtils.addAll(methods, clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(Class<?> clazz, T instance, String method, Class<?>[] argTypes,
                                             Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (argTypes == null) {
            argTypes = getArgTypes(arguments);
        }
        Method m = clazz.getMethod(method, argTypes);
        m.setAccessible(true);
        return m.invoke(instance, arguments);
    }

    /**
     * Returns array of the classes for the array of the objects
     */
    public static Class<?>[] getArgTypes(Object[] arguments) {
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (Object arg : arguments) {
            types.add(arg.getClass());
        }
        return types.toArray(new Class<?>[types.size()]);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Class<?>[] argTypes, Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeAnyMethod(instance.getClass(), instance, method, argTypes, arguments);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Object... args) throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        return invokeAnyMethod(instance, method, null, args);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static boolean isMethodExists(Class clazz, String method, Class<?>... argTypes) {
        try {
            clazz.getMethod(method, argTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
