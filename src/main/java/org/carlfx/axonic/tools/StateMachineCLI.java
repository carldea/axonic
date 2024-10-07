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

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.carlfx.axonic.tools.DiagramHelper.*;
import static org.carlfx.axonic.tools.DiagramHelper.toPlantUml;

public class StateMachineCLI {
    public static void beginConsoleSession(StateMachine stateMachine) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Here is a state pattern of a %s depicted here: ".formatted(stateMachine.getName()));
        System.out.println("\n" + toPlantUml(stateMachine));
        System.out.println(" NOTE: If you are in stuck state type: jump <my_state>. e.g. jump Locked");
        System.out.println("       Also to see all states type: show states");
        System.out.println("Press [h] for help.");
        System.out.println("Press [q] to quit.");
        System.out.println("   Your initial state is: " + stateMachine.currentState().getName());

        while (true) {
            askUser(stateMachine);
            String inputTransition = scanner.nextLine();
            System.out.println();
            // Validate input
            // quit
            if (inputTransition.trim().equalsIgnoreCase("q")) {
                System.out.println("Bye!");
                break;
            }
            if (inputTransition.trim().equals("h")) {
                System.out.println("""            
                         +-----------------------------------------------------+
                         |  Help menu                                          |
                         |                 h - Help menu                       |
                         |                 q - Quit                            |
                         |       show states - All states with state machine   |
                         |      jump <state> - Jump to a known state by name   |
                         |                     e.g. jump Locked                |
                         |                                                     |
                         |   diagram <xxxxx> - mermaid, plantuml               |
                         |                                                     |
                         | <transition name> or                                |
                         |     [line number] - type a transition name to       |
                         |                     advance. Optionally type the    |
                         |                     line number to transition.      |
                         |                                                     |
                         |  transition table - Show a transition table         |
                         +-----------------------------------------------------+
                         """);
                continue;
            }
            // show all states
            if (inputTransition.trim().startsWith("show states")) {
                System.out.println("Showing available states for " + stateMachine.getName());
                System.out.println(" States: [" + stateMachine.getStatePattern().states().stream().map(state -> state.getName()).collect(Collectors.joining(", ")) + "]");
                continue;
            }

            if (inputTransition.trim().equals("transition table")) {
                System.out.println(toTransitionTable(stateMachine));
                continue;
            }
            // display diagram
            if (inputTransition.trim().startsWith("diagram")) {
                String[] pair = inputTransition.split(" ");
                if (pair.length > 1) {
                    String diagram = pair[1];
                    if (diagram.equalsIgnoreCase("mermaid")) {
                        System.out.println("----------------------------------------");
                        System.out.println("Diagram %s %s".formatted(diagram, "https://mermaid.live/"));
                        System.out.println("----------------------------------------");
                        System.out.println("\n" + toMermaid(stateMachine));
                        System.out.println("----------------------------------------");
                    } else if (diagram.equalsIgnoreCase("plantuml")) {
                        System.out.println("----------------------------------------");
                        System.out.println("Diagram %s %s".formatted(diagram, "https://www.plantuml.com/plantuml/uml"));
                        System.out.println("----------------------------------------");
                        System.out.println("\n" + toPlantUml(stateMachine));
                        System.out.println("----------------------------------------");
                    }
                } else {
                    System.out.println("Invalid diagram, please try again.");
                }
                continue;
            }
            // if you are in a stuck state type: jump locked
            // this allows you to jump to any state.
            if (inputTransition.trim().startsWith("jump")) {
                String[] pair = inputTransition.split(" ");
                if (pair.length > 1) {
                    String jumpToState = pair[1];
                    System.out.println("Jumping to a new state " + jumpToState);
                    Optional<State> toStateOpt = stateMachine.lookupStateByName(jumpToState);
                    toStateOpt.ifPresentOrElse(state -> stateMachine.initial(state), ()->{
                        System.out.println("Invalid State to begin, please try again.");
                    });
                } else {
                    System.out.println("Invalid State to begin, please try again.");
                }
                System.out.println("Your initial state is: " + stateMachine.currentState().getName());
                continue;
            }

            // is transition typed a number
            String regex = "\\d+";
            if (inputTransition.matches(regex)) {
                int index = Integer.parseInt(inputTransition);
                List<Transition> transitions = stateMachine.outgoingTransitions();
                if (transitions != null && index > -1 && index < transitions.size()) {
                    Transition transition = transitions.get(index);
                    inputTransition = transition.name();
                }
                // else the transition maybe a number as a name.
            }
            // new support for input data when transitioning. if null the transition name is used.
            String firstChar = inputTransition.charAt(0)+""; // make some random input first character
            // if transition is not valid invoke code block. (BiConsumer<String, T>)
            stateMachine.tOrElse(inputTransition, firstChar, (invalidTName, input)->
                    System.out.println("Invalid choices, try again.")
            );

            System.out.println("transition: %s - input = %s".formatted(inputTransition, firstChar));
        }
    }
    public static void askUser(StateMachine stateMachine){
        System.out.println();
        System.out.println("Your current state is: " + stateMachine.currentState().getName());
        // prompt user options
        System.out.println("Where to go next? (Type the transition name or line number to move to the next state)");
        for (int i = 0; i < stateMachine.outgoingTransitions().size(); i++) {
            Transition transition = stateMachine.outgoingTransitions().get(i);
            System.out.println("%s) %s ---> (%s) ".formatted(i, transition.name(), transition.toState().getName()) );
        }
        System.out.println();
        System.out.print("Enter command or transition: ");
    }
}
