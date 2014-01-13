package ru.yandex.qatools.fsm.impl;

import ru.yandex.qatools.fsm.Yatomata;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class FSMBuilder<T> implements Yatomata.Builder<T> {
    final Class<T> fsmClass;

    public FSMBuilder(Class<T> fsmClass) {
        this.fsmClass = fsmClass;
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
            if (state == null) {
                return new YatomataImpl<>(fsmClass, fsmClass.newInstance());
            }
            return new YatomataImpl<>(fsmClass, fsmClass.newInstance(), state);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize the FSM Engine for FSM " + fsmClass, e);
        }
    }
}
