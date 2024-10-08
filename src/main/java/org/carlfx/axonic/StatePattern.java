/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright © 2024. Carl Dea.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.carlfx.axonic;

import java.util.*;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.STOP;

/**
 * A default StatePattern allowing a developer to define a state pattern.
 * Given a transition table describing all states and transitions.
 * An instance is used to create a state machine. A state pattern defines
 * a state diagram and a state machine manages a user state.
 */
public class StatePattern implements FSMBuilder {
    private List<Transition> transitions;
    private Set<State> states;
    private Map<State, List<Transition>> outgoingTransitions = new HashMap<>();
    boolean initCalled = false;
    private State currentState;

    /**
     * Default constructor.
     */
    public StatePattern() {
    }
    @Override
    public FSMBuilder initial(State state) {
        currentState = state;
        states().add(INITIAL);
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
    public FSMBuilder moveInitial(State state) {
        if (initCalled) {
            // remove from log
            Transition initialTransition = lookupOutgoingTransitions(INITIAL).get(0);
            transitions().remove(initialTransition);
            outgoingTransitions.remove(INITIAL);
        }

        currentState = state;
        states().add(state);
        Transition initialT = new Transition(INITIAL.name.toLowerCase(), INITIAL, state);
        transitions().add(initialT);
        outgoingTransitions.put(INITIAL, List.of(initialT));
        initCalled = true;

        return this;
    }

    @Override
    public FSMBuilder stop() {
        if (currentState().equals(STOP)) {
            throw new RuntimeException("Can not make consecutive stop transitions");
        }
        states().add(STOP);
        Transition stop = new Transition(STOP.name.toLowerCase(), currentState(), STOP);
        if (!transitions().contains(stop)) {
            transitions().add(stop);
        }
        outgoingTransitions.putIfAbsent(STOP, Collections.EMPTY_LIST);
        addOutgoingTransitionsByState(currentState(), stop);
        return this;
    }

    /**
     * Returns a list of outgoing transitions based on current state.
     * @param state current state.
     * @return Returns a list of outgoing transitions based on current state.
     */
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
        return this.t(name, (String) null);
    }

    @Override
    public FSMBuilder t(String name, String description) {
        return t(new Transition(name, currentState(), currentState(), description));
    }
    @Override
    public FSMBuilder t(Transition transition) {
        transitions().add(transition);
        addOutgoingTransitionsByState(transition.fromState(), transition);
        return this;
    }

    @Override
    public FSMBuilder t(String transition, State toState) {
        return t(transition, currentState(), toState);
    }

    @Override
    public FSMBuilder t(String transition, State toState, String description) {
        return t(transition, currentState(), toState, description);
    }

    @Override
    public FSMBuilder t(String transition, State fromState, State toState) {
        return t(transition, fromState, toState, null);
    }

    @Override
    public FSMBuilder t(String transition, State fromState, State toState, String description) {
        Transition transition1 = new Transition(transition, fromState, toState, description);
        states().add(fromState);
        states().add(toState);
        return t(transition1);
    }

    @Override
    public FSMBuilder s(State state) {
        currentState = state;
        states().add(state);
        Transition transition = null;
        if (transitions().size() > 0) {
            transition = transitions().remove(transitions().size() -1);
        }
        if (transition != null) {
            // Remove previous transition from lookup map.
            removeOutgoingTransitionsByState(transition.fromState(), transition);
            Transition recreatedTransition = transition.withToState(state);
            transitions().add(recreatedTransition);
            addOutgoingTransitionsByState(recreatedTransition.fromState(), recreatedTransition);
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
                "initCalled=" + initCalled +
                "states=" + states() +
                "transitions=" + transitions() +
                '}';
    }
}
