package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.Yatomata;

/**
 * @author Ilya Sadykov
 */
public class FSMBuilder<T> implements Yatomata.Builder<T> {
    private T instance;
    private Class<T> fsmClass;

    /**
     * Create FSM from class with no-arg constructor
     * @param fsmClass FSM class
     */
    public FSMBuilder(Class<T> fsmClass) {
        this.fsmClass = fsmClass;
    }

    /**
     * Use already instantiated FSM class. 
     * This can be e.g. used with Spring beans.
     * @param instance FSM instance
     */
    public FSMBuilder(T instance) {
        this.instance = instance;
    }

    /**
     * Build the new FSM engine with default state
     */
    @Override
    public Yatomata<T> build() {
        return build(null);
    }

    /**
     * Build the new FSM engine with the defined state
     */
    @Override
    public Yatomata<T> build(Object state) {
        try {
            T inst = (instance != null) ? instance : fsmClass.newInstance(); 
            if (state == null) {
                return new YatomataImpl<>(getFsmClass(), inst);
            }
            return new YatomataImpl<>(getFsmClass(), inst, state);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize the FSM Engine for FSM " + getFsmClass(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getFsmClass() {
        return (instance != null) ? (Class<T>) instance.getClass() : fsmClass;
    }
}
