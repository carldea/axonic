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
 * This allows Records, Enums, and Classes to be used as State objects for the state machine.
 */
public interface State {
    /**
     * State name
     * @return name of state
     */
    String getName();

    /**
     * A default empty string description is returned.
     * Implementor can override.
     * @return Description of the state.
     */
    default String getDescription(){
        return "";
    }
}
