package com.github.resource4j.resources.processors.parser;

import java.util.ArrayList;
import java.util.List;

public class StateBuilder<E extends Enum<E>, T extends Enum<T>> {

    private final Action<E, T> anyMatch;
    private List<Case<E, T>> cases = new ArrayList<>();
    private Action<E, T> finalMatch;
    private Action<E, T> onError;

    public StateBuilder(Action<E, T> anyMatch) {
        this.anyMatch = anyMatch;
    }

    @SafeVarargs
    public static <E extends Enum<E>, T extends Enum<T>> StateBuilder<E, T> aState(T transition, E... events) {
        return new StateBuilder<>(new ActionBuilder<>(transition, events).build());
    }

    @SafeVarargs
    public final StateBuilder<E, T> on(char c, T transition, E... events) {
        cases.add(new ActionBuilder<>(transition, events).buildCase(c));
        return this;
    }

    @SafeVarargs
    public final StateBuilder<E, T> onError(T transition, E... events) {
        this.onError = new ActionBuilder<>(transition, events).build();
        return this;
    }

    @SafeVarargs
    public final StateBuilder<E, T> whenDone(T transition, E... events) {
        this.finalMatch = new ActionBuilder<>(transition, events).build();
        return this;
    }

    @SuppressWarnings({"unchecked", "raw"})
    public State<E, T> build() {
        if (onError == null) {
            onError = anyMatch;
        }
        return new State<>(cases.toArray(new Case[0]), anyMatch, finalMatch, onError);
    }

}
