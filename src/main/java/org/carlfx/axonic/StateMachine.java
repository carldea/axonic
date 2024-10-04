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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.INVALID;

/**
 * A default implementation of a state machine. Manages state.
 */
public class StateMachine implements FSM {
    private final StatePattern statePattern;
    private State previousState;
    private State currentState;
    private Transition currentTransition;

    private final Map<State, List<Runnable>> stateCodeMap = new LinkedHashMap<>();
    private final Map<State, List<InputTransition<Transition, Object>>> inputStateCodeMap = new LinkedHashMap<>();

    private StateMachine(StatePattern statePattern) {
        this.statePattern = statePattern;
    }

    /**
     * Returns the state pattern.
     * @return Returns the state pattern.
     */
    public StatePattern getStatePattern() {
        return statePattern;
    }

    /**
     * Factory function to create a state machine given a state pattern.
     * @param statePattern State pattern defined.
     * @return Returns a StateMachine instance.
     */
    public static StateMachine create(StatePattern statePattern) {
        StateMachine stateMachine = new StateMachine(statePattern);
        stateMachine.previousState = INITIAL;
        stateMachine.currentTransition = statePattern.lookupOutgoingTransitions(INITIAL).getFirst();
        if (stateMachine.currentTransition == null) {
            throw new RuntimeException("StatePattern does not contain an initial transition");
        }
        stateMachine.currentState = stateMachine.currentTransition.toState();
        return stateMachine;
    }

    /**
     * Factory function to create a state machine given a state pattern.
     * @param statePatternConsumer allowing caller to define a state pattern.
     * @return Returns a StateMachine instance.
     */
    public static StateMachine create(Consumer<StatePattern> statePatternConsumer) {
        StatePattern statePattern = new StatePattern();
        statePatternConsumer.accept(statePattern);
        return create(statePattern);
    }

    @Override
    public FSM initial(State state) {
        statePattern.moveInitial(state);
        currentState = state;
        previousState = INITIAL;
        return this;
    }

    @Override
    public <T> FSM t(String transition, T input) {
        List<Transition> outTransitions = statePattern.lookupOutgoingTransitions(currentState());
        Optional<Transition> choice = outTransitions.stream().filter(t -> t.name().equals(transition)).findFirst();
        // current state find out transition if make previous = current and transition to as next state.
        choice.ifPresentOrElse( t -> {
                    // transition to next state.
                    previousState = currentState();
                    currentState = t.toState();
                    currentTransition = t;
                    List<Runnable> runnables = stateCodeMap.get(t.toState());
                    if (runnables != null) {
                        runnables.forEach(Runnable::run);
                    }
                    // This facility allows input
                    List<InputTransition<Transition, Object>> transitionInputConsumers = inputStateCodeMap.get(t.toState());
                    if (transitionInputConsumers != null) {
                        transitionInputConsumers.forEach(inputStateConsumer ->
                                // use the transition name as input
                            inputStateConsumer.accept(t, input==null ? t.name() : input)
                        );
                    }

                },
                ()-> currentState = INVALID); // don't set previous so caller can recover.
        // if (previous != current) call when (code blocks)
        return this;
    }
    @Override
    public FSM t(String transition) {
        return t(transition, null);
    }

    @Override
    public Transition currentTransition() {
        return currentTransition;
    }

    @Override
    public List<Transition> outgoingTransitions() {
        return statePattern.lookupOutgoingTransitions(currentState());
    }

    @Override
    public Optional<Transition> lookupNextTransition(String transitionName) {
        List<Transition> outTransitions = statePattern.lookupOutgoingTransitions(currentState());
        return outTransitions.stream().filter(t -> t.name().equals(transitionName)).findAny();
    }

    @Override
    public State currentState() {
        return currentState;
    }

    @Override
    public Optional<State> lookupStateByName(String name) {
        return getStatePattern().states().stream().filter(state -> state.getName().equals(name)).findAny();
    }

    @Override
    public State previousState() {
        return previousState;
    }

    @Override
    public FSM when(State state, Runnable codeBlock) {
        List<Runnable> list = stateCodeMap.get(state);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(codeBlock);
        stateCodeMap.put(state, list);
        return this;
    }

    @Override
    public <T> FSM when(State state, InputTransition<Transition, T> codeBlock){
        List<InputTransition<Transition, Object>> list = inputStateCodeMap.get(state);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add((InputTransition<Transition, Object>) codeBlock);
        inputStateCodeMap.put(state, list);

        return this;
    }

    @Override
    public FSM tOrElse(String transitionName, Runnable invalid) {
        Optional<Transition> transitionOpt = lookupNextTransition(transitionName);
        transitionOpt.ifPresentOrElse( transition -> t(transition.name()), invalid);
        return this;
    }

    @Override
    public <T> FSM tOrElse(String transitionName, T input, BiConsumer<String, T> invalid) {
        Optional<Transition> transitionOpt = lookupNextTransition(transitionName);
        transitionOpt.ifPresentOrElse( transition -> t(transition.name()),
                () ->  invalid.accept(transitionName, input));
        return this;
    }
}
