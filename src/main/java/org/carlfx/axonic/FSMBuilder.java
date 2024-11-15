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

import java.util.List;
import java.util.Set;

/**
 * Finite State Machine Builder defines a state pattern.
 * The state machine follows the Moore model as opposed to the Mealy.
 * State patterns are essential directed graphs with transitions that represent edges and states represent vertices.
 * An implementation will follow the builder pattern to assemble a state diagram.
 */
public interface FSMBuilder {

    /**
     * Returns a list of outgoing transitions based on current state.
     * @param state current state.
     * @return Returns a list of outgoing transitions based on current state.
     */
    List<Transition> lookupOutgoingTransitions(State state);

    /**
     * Add a new outgoing transition based on a state
     * @param state state
     * @param transition transition to add as an outgoing state transition.
     */
    FSMBuilder addOutgoingTransitionsByState(State state, Transition transition);

    /**
     * Remove an outgoing transition based on a state.
     * @param state state
     * @param transition transition to add as an outgoing state transition.
     */
    FSMBuilder removeOutgoingTransitionsByState(State state, Transition transition);

    /**
     * Transition line from and to a known state.
     * @param transition Name of transition
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition);

    /**
     * Transition line from and to a known state.
     * @param transition Name of transition
     * @param description description of transition
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition, String description);

    /**
     * Transition line from and to a known state.
     * @param transition Transition object.
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(Transition transition);

    /**
     * Transition line from current state to some state.
     * @param transition Transition object.
     * @param toState  next state
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition, State toState);

    /**
     * Transition line from current state to some state.
     * @param transition Transition object.
     * @param toState  next state
     * @param description description of transition.
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition, State toState, String description);
    /**
     * Transition line from and to a known state.
     * @param transition Transition object.
     * @param fromState previous state
     * @param toState  next state
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition, State fromState, State toState);

    /**
     * Transition line from and to a known state.
     * @param transition Transition object.
     * @param fromState previous state
     * @param toState  next state
     * @param description
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder t(String transition, State fromState, State toState, String description);

    /**
     * Establishes the next state for an outgoing transition.
     * @param state next state.
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder s(State state);

    /**
     * Return all transitions created.
     * @return Return all transitions created.
     */
    List<Transition> transitions();

    /**
     * Returns all states.
     * @return Returns all states.
     */
    Set<State> states();

    /**
     * Returns the current state when defining.
     * @return Returns the current state when defining.
     */
    State currentState();

    /**
     * Add an initial transition that begins with a state.
     * @param state The state to begin
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder initial(State state);

    /**
     * Moves the initial transition to a new state.
     * @param state target state to begin.
     * @return Moves the initial transition to a new state.
     */
    FSMBuilder moveInitial(State state);

    /**
     * Add a stop transition that ends the state machine.
     * @return The current FSMBuilder (state pattern) this allows method chaining.
     */
    FSMBuilder stop();
}
