package org.carlfx.axonic;

public record Transition(String name, State fromState, State toState) {
    public Transition withFromState(State fromState) {
        return new Transition(name(), fromState, toState());
    }
    public Transition withToState(State toState) {
        return new Transition(name(), fromState(), toState);
    }
}
