/*
 *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *
 *  * Copyright © 2024. Carl Dea.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.carlfx.axonic;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.STOP;

/**
 * Simple diagram utility class to output common graph notations.
 * Support for the following:
 * <pre>
 *     1. Mermaid https://mermaid.live/
 *     2. PlantUml https://www.plantuml.com/plantuml/uml
 * </pre>
 */
public class DiagramHelper {
    private DiagramHelper() {}
    /**
     * Based on the PlantUML live editor https://www.plantuml.com/plantuml/uml
     * @param stateMachine Axonic state machine
     * @return A PlantUML graph notation to represent digraph
     */
    public static String toPlantUml(StateMachine stateMachine) {
        String template = """
                @startuml
                %s@enduml
                """;
        StringBuilder sb = new StringBuilder();

        String pairString = "%s --> %s : %s\n";
        stateMachine.getStatePattern().transitions().forEach(transition -> {
            String fromS = transition.fromState() == INITIAL ? "[*]" : transition.fromState().getName();
            String toS = transition.toState() == STOP ? "[*]" : transition.toState().getName();
            sb.append(pairString.formatted(fromS, toS, transition.name()));
        });
        return template.formatted(sb.toString());
    }

    /**
     * Based on the Mermaid live editor https://mermaid.live/
     *
     * @param stateMachine Axonic state machine
     * @return A mermaid graph notation to represent digraph
     */
    public static String toMermaid(StateMachine stateMachine) {
        String template = """
                stateDiagram-v2
                %s
                """;
        StringBuilder sb = new StringBuilder();

        String pairString = "   %s --> %s : %s\n";
        stateMachine.getStatePattern().transitions().forEach(transition -> {
            String fromS = transition.fromState() == INITIAL ? "[*]" : transition.fromState().getName();
            String toS = transition.toState() == STOP ? "[*]" : transition.toState().getName();
            sb.append(pairString.formatted(fromS, toS, transition.name()));
        });
        return template.formatted(sb.toString());
    }
}
