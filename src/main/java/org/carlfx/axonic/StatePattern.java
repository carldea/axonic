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
    private boolean initCalled = false;
    private State currentState;

    /**
     * When the simple transition method is called. e.g. s.(MYSTATE1).t("north")
     * The from state or current state is based on the last call such as s.(state) or t.("north", STATE2)
     * This flag is use when the next function call is a current state change like the following:
     * <pre>
     *     s.(STATE_A)
     *     t.("north")
     *     s.(STATE_B)
     *
     *     means STATE_A has an outgoing transition to STATE_B.
     *
     *     s.(STATE_A)
     *     t.("north", STATE_C)
     *     s.(STATE_B)
     *     t.("east")
     *
     *     means STATE_A has an outgoing transition to STATE_C while
     *           STATE_B has an outgoing transition to STATE_B (itself) (remember last state change is the current state)
     *           prior to STATE_B, was STATE_C. t.("north", toState) or t.("north", fromState, toState) are advanced transitions
     *           and therefore will set the flag to false.
     *
     * </pre>
     */
    boolean simpleTransitionCalled = false;

    /**
     * Default constructor.
     */
    public StatePattern() {
    }
    @Override
    public StatePattern initial(State state) {
        currentState = state;
        states().add(INITIAL);
        states().add(state);
        if (!initCalled) {
            Transition initialT = new Transition(INITIAL.name.toLowerCase(), INITIAL, state);
            t(initialT);
            initCalled = true;
        } else {
            throw new RuntimeException("initial() already called. ");
        }
        return this;
    }

    @Override
    public StatePattern moveInitial(State state) {
        if (initCalled) {
            // remove from log
            Transition initialTransition = lookupOutgoingTransitions(INITIAL).get(0);
            transitions().remove(initialTransition);
            outgoingTransitions.remove(INITIAL);
        }

        currentState = state;
        states().add(state);
        Transition initialT = new Transition(INITIAL.name.toLowerCase(), INITIAL, state);
        transitions().add(0, initialT);
        outgoingTransitions.put(INITIAL, List.of(initialT));
        initCalled = true;

        return this;
    }

    @Override
    public StatePattern stop() {
        if (STOP.equals(currentState())) {
            throw new RuntimeException("Cannot make consecutive stop transitions");
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
    @Override
    public List<Transition> lookupOutgoingTransitions(State state) {
        List<Transition> transitionList = outgoingTransitions.get(state);
        if (transitionList == null) {
            transitionList = new ArrayList<>();
            outgoingTransitions.put(state, transitionList);
        }
        return transitionList;
    }

    /**
     * Add a new outgoing transition based on a state
     * @param state state
     * @param transition transition to add as an outgoing state transition.
     */
    @Override
    public StatePattern addOutgoingTransitionsByState(State state, Transition transition) {
        List<Transition> transitionList = lookupOutgoingTransitions(state);
        transitionList.add(transition);
        return this;
    }

    /**
     * Remove an outgoing transition based on a state.
     * @param state state
     * @param transition transition to add as an outgoing state transition.
     */
    @Override
    public StatePattern removeOutgoingTransitionsByState(State state, Transition transition) {
        List<Transition> transitionList = lookupOutgoingTransitions(state);
        transitionList.remove(transition);
        return this;
    }

    @Override
    public StatePattern t(String transition) {
        this.t(transition, (String) null);
        simpleTransitionCalled = true; // overloaded will set to false.
        return this;
    }

    @Override
    public StatePattern t(String transition, String description) {
        t(new Transition(transition, currentState(), currentState(), description));
        simpleTransitionCalled = true; // overloaded will set to false.
        return this;
    }

    /**
     * All transition definitions call this method ultimately.
     * @param transition Transition object.
     * @return StatePattern itself.
     */
    @Override
    public StatePattern t(Transition transition) {
        simpleTransitionCalled = false;
        if (STOP.equals(transition.fromState())) {
            throw new RuntimeException("The From state (previous) cannot be a STOP state");
        }
        if (INITIAL.equals(transition.toState())) {
            throw new RuntimeException("The From state (next) cannot be a INITIAL state");
        }
        transitions().add(transition);
        if (transition.fromState() != null) {
            states().add(transition.fromState());
        }
        if (transition.toState() != null) {
            states().add(transition.toState());
        }

        addOutgoingTransitionsByState(transition.fromState(), transition);
        // Note: Not allowed to create a From state of STOP on a transition
        //       This will make the current state the previous valid non STOP state.
        if (STOP.equals(transition.toState())) {
            // use the From state
            currentState = transition.fromState();
        } else {
            // valid. use the To state.
            currentState = transition.toState();
        }
        return this;
    }

    @Override
    public StatePattern t(String transition, State toState) {
        simpleTransitionCalled = false;
        t(transition, currentState(), toState);
        return this;
    }

    @Override
    public StatePattern t(String transition, State toState, String description) {
        simpleTransitionCalled = false;
        t(transition, currentState(), toState, description);
        return this;
    }

    @Override
    public StatePattern t(String transition, State fromState, State toState) {
        simpleTransitionCalled = false;
        return t(transition, fromState, toState, null);
    }

    @Override
    public StatePattern t(String transition, State fromState, State toState, String description) {
        simpleTransitionCalled = false;
        Transition transition1 = new Transition(transition, fromState, toState, description);
        states().add(fromState);
        states().add(toState);
        return t(transition1);
    }

    @Override
    public StatePattern s(State state) {
        currentState = state;
        states().add(state);
        Transition transition = null;

        // If previous call was a simple transition e.g. .t("north")
        if (simpleTransitionCalled) {
            simpleTransitionCalled = false; // reset
            if (transitions().size() > 0) {
                transition = transitions().remove(transitions().size() - 1);
            }
            if (transition != null) {
                // Remove previous transition from lookup map.
                removeOutgoingTransitionsByState(transition.fromState(), transition);
                Transition recreatedTransition = transition.withToState(state);
                transitions().add(recreatedTransition);
                addOutgoingTransitionsByState(recreatedTransition.fromState(), recreatedTransition);
            }
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
