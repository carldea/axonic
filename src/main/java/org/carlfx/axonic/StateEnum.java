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
 * Default States available such as an Initial and Stop state. If an illegal transition occurs the INVALID state is used.
 */
public enum StateEnum implements State {
    /**
     * Initial state
     */
    INITIAL("Initial"),
    /**
     * Stop state
     */
    STOP("Stop"),
    /**
     * Invalid state
     */
    INVALID("Invalid");

    /**
     * name of the state
     */
    final String name;

    /**
     * A friendly state name
     * @param name
     */
    StateEnum(String name){
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}
