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
package org.carlfx.axonic.test;

import org.carlfx.axonic.State;
import org.carlfx.axonic.StateMachine;
import org.carlfx.axonic.StatePattern;
import org.carlfx.axonic.Transition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.carlfx.axonic.DiagramHelper.toMermaid;
import static org.carlfx.axonic.DiagramHelper.toPlantUml;
import static org.carlfx.axonic.test.StateBuilderTest.SensorState.*;
import static org.carlfx.axonic.test.TurnStyleState.*;

@DisplayName("StateBuilder Test")
public class StateBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(StateBuilderTest.class);

    public enum SensorState implements State {
        OPENED("Opened"),
        CLOSING("Closing"),
        OPENING("Opening"),
        CLOSED("Closed");

        final String name;

        SensorState(String name){
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }


    @Test
    @DisplayName("State Building Test Turn style")
    void stateBuildingTest() {
/*
----------------------
Mermaid state diagram
----------------------
stateDiagram-v2
    [*] --> Locked : initial
    Locked --> Locked: push
    Locked --> Unlocked : coin
    Unlocked --> Unlocked : coin
    Unlocked --> Locked : push

*/
        // input -> add fqn -> adding (add fqn form) -> done -> fqn already added state -> edit -> editting fqn -> cancel
        StateMachine turnStyleSM = StateMachine.create(statePattern ->
            statePattern
                    .initial(LOCKED)
                    .t("push")
                    .t("coin")
                    .s(UNLOCKED)
                    .t("coin")
                    .t("push")
                    .s(LOCKED)
        );
        turnStyleSM.when(UNLOCKED, (t, input) -> {
            LOG.info("transition: %s, input: %s, prevState: %s".formatted(t.name(), input, t.fromState()));
        });
        // test 1. see current state info
        debugInfo(turnStyleSM);

        // test 2. transition with push
        turnStyleSM.t("push", "p 2");
        debugInfo(turnStyleSM);

        // test 3. transition with coin
        turnStyleSM.t("coin", "c 3");
        debugInfo(turnStyleSM);

        // test 4. transition with coin
        turnStyleSM.t("coin", "c 4");
        debugInfo(turnStyleSM);

        // test 5. transition with push
        turnStyleSM.t("push", "p 5");
        debugInfo(turnStyleSM);

        // A turn stile
        StatePattern turnStyle = new StatePattern();

        turnStyle.initial(LOCKED)
                .t("push")
                .t("coin")
                .s(UNLOCKED)
                .t("coin")
                .t("push")
                .s(LOCKED);

        StateMachine turnStyleSM2 = StateMachine.create(turnStyle);

        // test 1. see current state info
        debugInfo(turnStyleSM2);

        // test 2. transition with push
        turnStyleSM2.t("push");
        debugInfo(turnStyleSM2);

        // test 3. transition with coin
        turnStyleSM2.t("coin");
        debugInfo(turnStyleSM2);

        // test 4. transition with coin
        turnStyleSM2.t("coin");
        debugInfo(turnStyleSM2);

        // test 5. transition with push
        turnStyleSM2.t("push");
        debugInfo(turnStyleSM2);

        toPlantUml(turnStyleSM2);
    }

    @Test
    @DisplayName("State Building Test sensors")
    void stateBuildingTest2() {
/*
----------------------
Mermaid state diagram
----------------------
stateDiagram-v2
    [*] --> Opened : initial
    Opened --> Closing: close
    Closing --> Opening : open
    Opening --> Closing : close
    Opening --> Opened : sensor opened
    closing --> closed : sensor closed
    closed --> opening : open

*/
        StateMachine sensorSM = StateMachine.create(statePattern ->
                statePattern
                        .initial(OPENED)
                        .t("close")
                        .s(SensorState.CLOSING)
                        .t("open")
                        .s(SensorState.OPENING)
                        .t("close")
                        .s(SensorState.CLOSING)
                        .t("sensor closed")
                        .s(SensorState.CLOSED)
                        .t("open")
                        .s(SensorState.OPENING)
                        .t("sensor opened")
                        .s(OPENED)
        );
        sensorSM.when(OPENED, () ->  LOG.info("OPENED"));
        sensorSM.when(OPENED, () ->  LOG.info("OPENED again"));

        sensorSM.when(CLOSING, () -> LOG.info("CLOSING"));
        sensorSM.when(OPENING, () -> LOG.info("opening state."));
        // test 1. see current state info
        debugInfo(sensorSM);
        //  state  |  transition (input) |
        sensorSM.t("add fqn"); // do you want to continue adding FQN.
        /// pattern detail / property panel/ add fqn/ Node UI adding fqn/ edit othername/ editing an othername/ add fqn/ adding existing?
        //sensorSM.tOrElse("asdfasdfasfd", () -> { report error to user});
        // test 2. transition with close
        sensorSM.t("close");
        debugInfo(sensorSM);

        // test 3. transition with open
        sensorSM.t("open");
        debugInfo(sensorSM);

        // test 4. transition with close
        sensorSM.t("close");
        debugInfo(sensorSM);

        // test 5. transition with sensor closed
        sensorSM.t("sensor closed");
        debugInfo(sensorSM);

        // test 6. transition with push
        sensorSM.t("open");
        debugInfo(sensorSM);
        
        LOG.info(toPlantUml(sensorSM));
    }
    private void debugInfo(StateMachine turnStyleSM) {
        LOG.info(" Chose transition: " + turnStyleSM.currentTransition());
        LOG.info("    Current state: " + turnStyleSM.currentState());
        LOG.info("       Prev state: " + turnStyleSM.previousState());
        LOG.info("Avail transitions: " + turnStyleSM.outgoingTransitions());
        LOG.info("-----------------------------------------------------");
    }

    private static StateMachine createSensorSM() {
        StateMachine sensorSM = StateMachine.create(statePattern ->
                statePattern
                        .initial(OPENED)
                        .t("close")
                        .s(SensorState.CLOSING)
                        .t("open")
                        .s(SensorState.OPENING)
                        .t("close")
                        .s(SensorState.CLOSING)
                        .t("sensor closed")
                        .s(SensorState.CLOSED)
                        .t("open")
                        .s(SensorState.OPENING)
                        .t("sensor opened")
                        .s(OPENED)
        );
        sensorSM.when(OPENED, () ->  LOG.info("OPENED"));
        sensorSM.when(OPENED, () ->  LOG.info("OPENED again"));

        sensorSM.when(CLOSING, () -> LOG.info("CLOSING"));
        sensorSM.when(OPENING, () -> LOG.info("opening state."));
        return sensorSM;
    }
    private static StateMachine createTurnStyleSM() {
        StateMachine turnStyleSM = StateMachine.create(statePattern ->
                statePattern
                        .initial(LOCKED)
                        .t("push")
                        .t("coin")
                        .s(UNLOCKED)
                        .t("coin")
                        .t("push")
                        .s(LOCKED)
                        .t("hello")
                        .s(FRED)
                        .t("hello2")
                        .moveInitial(LOCKED)
        );

        turnStyleSM
                .when(LOCKED, (t, input) -> LOG.info("Secured Can not enter. called %s from state %s, input=%s".formatted(t.name(), t.fromState(), input)))
                .when(UNLOCKED, () -> {
                    if (turnStyleSM.previousState() == LOCKED) LOG.info("You may enter");
                    if (turnStyleSM.previousState() == UNLOCKED) LOG.info("Thank you for more money!");
                });
        return turnStyleSM;
    }
    public static <T> void stateMachineCli(StateMachine stateMachine, String stateMachineName) {
        Scanner scanner = new Scanner(System.in);
        LOG.info("Here is a state pattern of a %s depicted here: ".formatted(stateMachineName));
        LOG.info("\n" + toPlantUml(stateMachine));
        LOG.info(" NOTE: If you are in stuck state type: jump <my_state>. e.g. jump Locked");
        LOG.info("       Also to see all states type: show states");
        LOG.info("Press [h] for help.");
        LOG.info("Press [q] to quit.");
        LOG.info("   Your initial state is: " + stateMachine.currentState().getName());

        while (true) {
            askUser(stateMachine);
            String inputTransition = scanner.nextLine();
            // Validate input
            // quit
            if (inputTransition.trim().equalsIgnoreCase("q")) {
                LOG.info("Bye!");
                System.exit(0);
            }
            if (inputTransition.trim().equals("h")) {

                LOG.info("""
                         
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
                         +-----------------------------------------------------+
                         """);
            }
            // show all states
            if (inputTransition.trim().startsWith("show states")) {
                LOG.info("Showing available states for " + stateMachineName);
                LOG.info(" States: [" + stateMachine.getStatePattern().states().stream().map(state -> state.getName()).collect(Collectors.joining(", ")) + "]");
                continue;
            }

            // display diagram
            if (inputTransition.trim().startsWith("diagram")) {
                String[] pair = inputTransition.split(" ");
                if (pair.length > 1) {
                    String diagram = pair[1];
                    if (diagram.equalsIgnoreCase("mermaid")) {
                        LOG.info("----------------------------------------");
                        LOG.info("Diagram %s %s".formatted(diagram, "https://mermaid.live/"));
                        LOG.info("----------------------------------------");
                        LOG.info("\n" + toMermaid(stateMachine));
                        LOG.info("----------------------------------------");
                    } else if (diagram.equalsIgnoreCase("plantuml")) {
                        LOG.info("----------------------------------------");
                        LOG.info("Diagram %s %s".formatted(diagram, "https://www.plantuml.com/plantuml/uml"));
                        LOG.info("----------------------------------------");
                        LOG.info("\n" + toPlantUml(stateMachine));
                        LOG.info("----------------------------------------");
                    }
                } else {
                    LOG.info("Invalid diagram, please try again.");
                }
                continue;
            }
            // if you are in a stuck state type: jump locked
            // this allows you to jump to any state.
            if (inputTransition.trim().startsWith("jump")) {
                String[] pair = inputTransition.split(" ");
                if (pair.length > 1) {
                    String jumpToState = pair[1];
                    LOG.info("Jumping to a new state " + jumpToState);
                    Optional<State> toStateOpt = stateMachine.lookupStateByName(jumpToState);
                    toStateOpt.ifPresentOrElse(state -> stateMachine.initial(state), ()->{
                        LOG.info("Invalid State to begin, please try again.");
                    });
                } else {
                    LOG.info("Invalid State to begin, please try again.");
                }
                LOG.info("Your initial state is: " + stateMachine.currentState().getName());
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
//            turnStyleSM.tOrElse(inputTransition,
//                    ()-> LOG.info("Invalid choices, try again."));

            // new support for input data when transitioning. if null the transition name is used.
            String firstChar = inputTransition.charAt(0)+""; // make some random input first character
            // if transition is not valid invoke code block. (BiConsumer<String, T>)
            stateMachine.tOrElse(inputTransition, firstChar, (invalidTName, input)->
                    LOG.info("Invalid choices, try again.")
            );

            LOG.info("transition: %s - input = %s".formatted(inputTransition, firstChar));

//            // prompt user options
//            askUser(stateMachine);
        }
    }
    public static void askUser(StateMachine stateMachine){
        LOG.info("Your current state is: " + stateMachine.currentState().getName());
        // prompt user options
        LOG.info("Where to go next? (Type the transition name or line number to move to the next state)");
        for (int i = 0; i < stateMachine.outgoingTransitions().size(); i++) {
            Transition transition = stateMachine.outgoingTransitions().get(i);
            LOG.info("%s) %s ---> (%s) ".formatted(i, transition.name(), transition.toState().getName()) );
        }
    }

    public static void main(String[] args){

        // A turn style test.
        StateMachine stateMachine = createTurnStyleSM();
        stateMachineCli(stateMachine, "Turn style");

        // Sensor state machine
//        StateMachine stateMachine = createSensorSM();
//        stateMachineCli(stateMachine, "Sensor StateMachine");

    }

}
