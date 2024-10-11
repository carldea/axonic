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

package org.carlfx.axonic.tools;

import org.carlfx.axonic.State;
import org.carlfx.axonic.StateMachine;
import org.carlfx.axonic.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        String colorForCurrentState = "#palegreen";
        String colorForCurrentTransitionLine = "[#green]";
        String template = """
                @startuml
                %s@enduml
                """;
        StringBuilder sb = new StringBuilder();
        // generate states and transitions
        String pairString = "%s -%s-> %s : %s\n";
        stateMachine.getStatePattern().transitions().forEach(transition -> {
            String fromS = transition.fromState() == INITIAL ? "[*]" : transition.fromState().getName();
            String toS = transition.toState() == STOP ? "[*]" : transition.toState().getName();
            // Color the current transition line
            String currentTransitionStyle = "";
            if (!INITIAL.equals(transition.fromState())
                    && transition.equals(stateMachine.currentTransition())){
                currentTransitionStyle = colorForCurrentTransitionLine;
            }
            sb.append(pairString.formatted(fromS, currentTransitionStyle, toS, transition.name()));
        });

        // generate styling for current states
        stateMachine.getStatePattern().states().forEach(state -> {

            State currentState = stateMachine.currentState();
            if (INITIAL.equals(state) || STOP.equals(state)) {
                // ignore styling
            } else if (state.equals(currentState)) {
                // style box
                String styleCurrentState = "state %s %s : %s\n"
                        .formatted(currentState.getName(), colorForCurrentState, currentState.getDescription());
                sb.append(styleCurrentState);
            } else {
                // name and description
                sb.append("state %s : %s\n".formatted(state.getName(), state.getDescription()));
            }

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

    public static String toTransitionTable(StateMachine stateMachine) {
        StringBuilder sb = new StringBuilder();
        List<String> headerList = new ArrayList<>();
        headerList.add("State");
        // for each transition by name create headers (unique by name)
        stateMachine
                .getStatePattern()
                .transitions()
                .forEach(transition -> {
                    if (!headerList.contains(transition.name())) {
                        headerList.add(transition.name());
                    }}
                );
        // define table array
        String[][] data = new String[stateMachine.getStatePattern().states().size()][headerList.size()];
        Set<State> states = stateMachine.getStatePattern().states();
        int i = 0;
        // for each state build table by rows.
        for(State state:states) {
            data[i][0] = state.getName(); // first column is the state

            // rest of the column headers are transition names.
            List<Transition> transitions = stateMachine.getStatePattern().lookupOutgoingTransitions(state);
            for(int j=1; j<headerList.size(); j++) {
                final int stateRow = i;
                final int nextTransition = j;
                Optional<Transition> transitionOpt = transitions.stream().filter(t -> t.name().equals(headerList.get(nextTransition))).findAny();

                // if found display next state to transition otherwise put an 'X' in the cell.
                transitionOpt.ifPresentOrElse(transition ->
                                data[stateRow][nextTransition] = transition.toState().getName(),
                        () ->  data[stateRow][nextTransition] = "X"
                );
            }
            i++; // next row
        }

        // build headers
        return ConsoleTable.printTable(headerList.toArray(new String[0]), data);
    }
}

final class ConsoleTable {
    public static void main(String[] args) {
        String[] headers = {"ID", "Name", "Age"};
        String[][] data = {
                {"1", "John", "25"},
                {"2", "Mary", "30"},
                {"3", "Bob", "20"}
        };

        System.out.println(printTable(headers, data));
    }

    public static String printTable(String[] headers, String[][] data) {
        int[] columnWidths = calculateColumnWidths(headers, data);

        StringBuilder sb = new StringBuilder();
        // Print header
        sb.append(printRow(headers, columnWidths));
        sb.append(printSeparator(columnWidths));

        // Print data rows
        for (String[] row : data) {
            sb.append(printRow(row, columnWidths));
        }
        return sb.toString();
    }

    private static int[] calculateColumnWidths(String[] headers, String[][] data) {
        int[] columnWidths = new int[headers.length];

        // Calculate width for headers
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        // Calculate width for data
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                columnWidths[i] = Math.max(columnWidths[i], row[i].length());
            }
        }

        // Add padding
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2; // 2 spaces padding on each side
        }

        return columnWidths;
    }

    private static String printRow(String[] row, int[] columnWidths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            String format = "%-" + columnWidths[i] + "s";
            sb.append(format.formatted(row[i]));
        }
        sb.append("\n");
        return sb.toString();
    }

    private static String printSeparator(int[] columnWidths) {
        StringBuilder sb = new StringBuilder();
        for (int width : columnWidths) {
            for (int i = 0; i < width; i++) {
                sb.append("-");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}

