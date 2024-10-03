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

/**
 * A transition object representing its name, a from and to state, and an optional description.
 * @param name Name of transition
 * @param fromState The from state.
 * @param toState The to state.
 * @param description The description of the transition. Some diagram tools allow notes or comments.
 */
public record Transition(String name, State fromState, State toState, String description) {

    /**
     * A transition object representing its name, a from and to state.
     * @param name Name of transition
     * @param fromState The from state.
     * @param toState The to state.
     */
    public Transition(String name, State fromState, State toState) {
        this(name, fromState, toState, null);
    }

    /**
     * Create a new instance of a transition with a name. A copy constructor.
     * @param name name of transition
     * @return  A new Transition record.
     */
    public Transition withName(String name) {
        return new Transition(name, fromState(), toState(), description());
    }

    /**
     * Create a new instance of a transition with a 'from' state. A copy constructor.
     * @param fromState the from state.
     * @return A new Transition record.
     */
    public Transition withFromState(State fromState) {
        return new Transition(name(), fromState, toState(), description());
    }

    /**
     * Create a new instance of a transition with a 'to' state. A copy constructor.
     * @param toState the outgoing state.
     * @return A new Transition record.
     */
    public Transition withToState(State toState) {
        return new Transition(name(), fromState(), toState, description());
    }

    /**
     * Create a new instance of a transition with a description.
     * @param description the description
     * @return A new Transition record.
     */
    public Transition withDescription(String description) {
        return new Transition(name(), fromState(), toState(), description);
    }

}
