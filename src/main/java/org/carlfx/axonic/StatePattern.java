package org.carlfx.axonic;

import java.util.*;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.STOP;

public class StatePattern implements FSMBuilder {
    private List<String> log = new ArrayList<>();
    private List<Transition> transitions;
    private Set<State> states;
    private Map<State, List<Transition>> outgoingTransitions = new HashMap<>();
    boolean initCalled = false;
    private State currentState;
    @Override
    public FSMBuilder initial(State state) {
        log.add(" i->" + state);
        currentState = state;
        states().add(state);
        if (!initCalled) {
            Transition initialT = new Transition(INITIAL.name.toLowerCase(), INITIAL, state);
            transitions().add(initialT);
            outgoingTransitions.put(INITIAL, List.of(initialT));
            initCalled = true;
        } else {
            throw new RuntimeException("initial() already called. ");
        }
        return this;
    }

    @Override
    public FSMBuilder stop() {
        if (currentState().equals(STOP)) {
            throw new RuntimeException("Can not make consecutive stop transitions");
        }
        log.add(currentState() + " -> " + STOP.name);
        states().add(STOP);
        Transition stop = new Transition(STOP.name.toLowerCase(), currentState(), STOP);
        if (!transitions().contains(stop)) {
            transitions().add(stop);
        }
        outgoingTransitions.putIfAbsent(STOP, Collections.EMPTY_LIST);
        addOutgoingTransitionsByState(currentState(), stop);
        return this;
    }

    public List<Transition> lookupOutgoingTransitions(State state) {
        List<Transition> transitionList = outgoingTransitions.get(state);
        if (transitionList == null) {
            transitionList = new ArrayList<>();
            outgoingTransitions.put(state, transitionList);
        }
        return transitionList;
    }
    private void addOutgoingTransitionsByState(State state, Transition transition) {
        List<Transition> transitionList = lookupOutgoingTransitions(state);
        transitionList.add(transition);
    }
    private void removeOutgoingTransitionsByState(State state, Transition transition) {
        List<Transition> transitionList = lookupOutgoingTransitions(state);
        transitionList.remove(transition);
    }
    @Override
    public FSMBuilder t(String name) {
        log.add(" %s-> (%s)".formatted(name, currentState()));
        Transition transition = new Transition(name, currentState(), currentState());
        transitions().add(transition);
        addOutgoingTransitionsByState(currentState(), transition);
        return this;
    }
    @Override
    public FSMBuilder t(Transition transition) {
        log.add(" %s-> (%s)".formatted(transition.name(), currentState));
        transitions().add(transition);
        addOutgoingTransitionsByState(currentState(), transition);
        return this;
    }

    @Override
    public FSMBuilder s(State state) {
        currentState = state;
        states().add(state);
        Transition transition = transitions().removeLast();
        if (transition != null) {
            // Remove previous transition from lookup map.
            removeOutgoingTransitionsByState(transition.fromState(), transition);

            String updating = log.removeLast();
            System.out.println("removing " + updating);
            Transition recreatedTransition = transition.withToState(state);
            transitions().add(recreatedTransition);
            addOutgoingTransitionsByState(recreatedTransition.fromState(), recreatedTransition);

            log.add(" %s-> (%s)".formatted(transition.name(), currentState));
        }
        return this;
    }

    @Override
    public List<Transition> transitions() {
        if (transitions == null) {
            transitions = new ArrayList<>();
        }
        return transitions;
    }

    @Override
    public Set<State> states() {
        if (states == null) {
            states = new HashSet<>();
        }
        return states;
    }

    @Override
    public State currentState() {
        return currentState;
    }

    @Override
    public String toString() {
        return "StatePattern{" +
                "log=" + log +
                "states=" + states() +
                "transitions=" + transitions() +
                '}';
    }
    public String generateDiagram() {
        /*
@startuml
[*] --> Opened : initial
Opened --> Closing: close
Closing --> Opening : open
Opening --> Closing : close
Closing --> Closed : sensor closed
Closed --> Opening : open
Opening --> Opened : sensor opened
@enduml
         */
        return null;
    }
}
