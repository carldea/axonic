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
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Finite State Machine maintains the state based on a state pattern.
 * The state machine follows the Moore model as opposed to the Mealy.
 * Instead of outputs on each state code blocks get invoked. Transitions
 * each have a name with an optional input to the outgoing state. As a
 * transition is occurring the developer can optionally use input for a
 * transition. If not specified the name of the transition will be used.
 */
public interface FSM {
    /**
     * Moves the initial transition to any state.
     * @param state The state to begin flow.
     * @return The current FSM (finite state machine) this allows method chaining.
     */
    FSM initial(State state);
    /**
     * Transitions to next state (outgoing state)
     * @param transition The transition name.
     * @return The current FSM (finite state machine) this allows method chaining.
     */
    FSM t(String transition);

    /**
     * If a transition is invalid invoke code block (Runnable).
     * @param transition the name of the transition.
     * @param invalid A code block to be run if transition is not valid.
     * @return The current FSM (finite state machine) this allows method chaining.
     */
    FSM tOrElse(String transition, Runnable invalid);
    /**
     * Transitions to next state (outgoing state) with an optional input. If input is null the name is used.
     * @param transition The transition name.
     * @param input Some input data passed to next State.
     * @return The current FSM (finite state machine) this allows method chaining.
     * @param <T> The input object type.
     */
    <T> FSM t(String transition, T input);
    /**
     * If a transition is invalid invoke code block (InputTransition).
     * @param transition the name of the transition.
     * @param input the transition's input.
     * @param invalid A code block to be run if transition is not valid. first parameter is the name of the transition and second is the input value.
     * @return The current FSM (finite state machine) this allows method chaining.
     * @param <T> The input object type.
     *
     */
    <T> FSM tOrElse(String transition, T input, BiConsumer<String, T> invalid);

    /**
     * Returns the current transition.
     * @return Transition a transition has a from state and to state.
     */
    Transition currentTransition();

    /**
     * Returns an Optional containing a Transition instance.
     * @param transitionName the name of an outgoing transition name of the current state.
     * @return Returns an Optional containing a Transition instance.
     */
    Optional<Transition> lookupNextTransition(String transitionName);
    /**
     * Outgoing transitions.
     * @return A list of transition names.
     */
    List<Transition> outgoingTransitions();

    /**
     * Returns the current state in the state machine.
     * @return Returns the current state in the state machine.
     */
    State currentState();

    /**
     * Returns the previous state in the state machine.
     * @return Returns the previous state in the state machine.
     */
    State previousState();

    /**
     * Lookup a state based on its name.
     * @param name name of state.
     * @return State is returned.
     */
    Optional<State> lookupStateByName(String name);

    /**
     * When an encountered state code can get invoked. Code block will receive the called transition and the input value.
     * If input value is null the transition name is used.
     * @param state A state encountered.
     * @param codeBlock Code to be invoked when state is encountered.
     * @return The current FSM (finite state machine) this allows method chaining.
     * @param <T> T is the type of the input value when transitioning.
     */
    <T> FSM when(State state, InputTransition<Transition, T> codeBlock);

    /**
     * When an encountered state code can get invoked.
     * @param state A state encountered.
     * @param codeBlock Code to be invoked when state is encountered.
     * @return The current FSM (finite state machine) this allows method chaining.
     */
    FSM when(State state, Runnable codeBlock);
}
