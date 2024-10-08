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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.carlfx.axonic.StateEnum.STOP;
import static org.carlfx.axonic.tools.DiagramHelper.*;
import static org.carlfx.axonic.test.StateBuilderTest.SensorState.*;
import static org.carlfx.axonic.test.TurnstileState.*;
import static org.carlfx.axonic.tools.StateMachineCLI.beginConsoleSession;

@DisplayName("StateBuilder Test")
public class StateBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(StateBuilderTest.class);

    public enum SensorState implements State {
        OPENED("Opened"),
        CLOSING("Closing"),
        OPENING("Opening"),
        CLOSED("Closed"),
        WILMA("Wilma");

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
    @DisplayName("State Building Test Turnstile")
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
        StateMachine turnstileSM = StateMachine.create(statePattern ->
            statePattern
                    .initial(LOCKED)
                    .t("push")
                    .t("coin")
                    .s(UNLOCKED)
                    .t("coin")
                    .t("push")
                    .s(LOCKED)
        );
        turnstileSM.when(UNLOCKED, (t, input) -> {
            System.out.println("transition: %s, input: %s, prevState: %s".formatted(t.name(), input, t.fromState()));
        });
        // test 1. see current state info
        debugInfo(turnstileSM);
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());

        // test 2. transition with push
        turnstileSM.t("push", "p 2");
        debugInfo(turnstileSM);
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());

        // test 3. transition with coin
        turnstileSM.t("coin", "c 3");
        debugInfo(turnstileSM);
        Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

        // test 4. transition with coin
        turnstileSM.t("coin", "c 4");
        debugInfo(turnstileSM);
        Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

        // test 5. transition with push
        turnstileSM.t("push", "p 5");
        debugInfo(turnstileSM);
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());
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
                        .s(CLOSING)
                        .t("open")
                        .s(OPENING)
                        .t("close")
                        .s(CLOSING)
                        .t("sensor closed")
                        .s(CLOSED)
                        .t("open")
                        .s(OPENING)
                        .t("sensor opened")
                        .s(OPENED)
        );
        sensorSM.when(OPENED, () ->  System.out.println("OPENED"));
        sensorSM.when(OPENED, () ->  System.out.println("OPENED again"));

        sensorSM.when(CLOSING, () -> System.out.println("CLOSING"));
        sensorSM.when(OPENING, () -> System.out.println("opening state."));
        // test 1. see current state info
        debugInfo(sensorSM);
        Assertions.assertEquals(OPENED, sensorSM.currentState());

        // test 2. transition with close
        sensorSM.t("close");
        debugInfo(sensorSM);
        Assertions.assertEquals(CLOSING, sensorSM.currentState());

        // test 3. transition with open
        sensorSM.t("open");
        debugInfo(sensorSM);
        Assertions.assertEquals(OPENING, sensorSM.currentState());

        // test 4. transition with close
        sensorSM.t("close");
        debugInfo(sensorSM);
        Assertions.assertEquals(CLOSING, sensorSM.currentState());

        // test 5. transition with sensor closed
        sensorSM.t("sensor closed");
        debugInfo(sensorSM);
        Assertions.assertEquals(CLOSED, sensorSM.currentState());

        // test 6. transition with push
        sensorSM.t("open");
        debugInfo(sensorSM);
        Assertions.assertEquals(OPENING, sensorSM.currentState());
        
        System.out.println(toPlantUml(sensorSM));
    }
    private void debugInfo(StateMachine turnstileSM) {
        LOG.info(" Chose transition: " + turnstileSM.currentTransition());
        LOG.info("    Current state: " + turnstileSM.currentState());
        LOG.info("       Prev state: " + turnstileSM.previousState());
        LOG.info("Avail transitions: " + turnstileSM.outgoingTransitions());
        LOG.info("-----------------------------------------------------");
    }

    private static StateMachine createSensorSM() {
        StateMachine sensorSM = StateMachine.create("Sensor State Machine", statePattern ->
                statePattern
                        .initial(OPENED)
                        .t("close")
                        .s(CLOSING)
                        .t("open")
                        .s(OPENING)
                        .t("close")
                        .s(CLOSING)
                        .t("sensor closed")
                        .s(CLOSED)
                        .t("open")
                        .s(OPENING)
                        .t("sensor opened")
                        .s(OPENED)
                        .t("boo", OPENING, CLOSED)   // from OPENING -boo-> CLOSED
                        .t("boo2", CLOSING, OPENING) // from CLOSING -boo2-> OPENING
                        .s(CLOSED)
                        .t("boo", WILMA, "Fred's wife") // from CLOSED -boo-> WILMA
                        .t("stop", WILMA, STOP)

        );
        sensorSM.when(OPENED, () ->  System.out.println("OPENED"));
        sensorSM.when(OPENED, () ->  System.out.println("OPENED again"));

        sensorSM.when(CLOSING, () -> System.out.println("CLOSING"));
        sensorSM.when(OPENING, () -> System.out.println("opening state."));
        return sensorSM;
    }
    private static StateMachine createTurnstileSM() {
        StateMachine turnstileSM = StateMachine.create("Turnstile", statePattern ->
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
        );

        turnstileSM
                .when(LOCKED, (t, input) -> System.out.println("Secured. You may not enter. Transition %s from state %s, input=%s".formatted(t.name(), t.fromState(), input)))
                .when(UNLOCKED, (t, input) -> {
                    if (turnstileSM.previousState() == LOCKED) System.out.println("You may enter");
                    if (turnstileSM.previousState() == UNLOCKED) System.out.println("Thank you for more money!");
                    if (!t.name().equals(input)) System.out.println("Input = " + input);
                })
                .when(FRED, () -> System.out.println("We are stuck. use jump <state>"));
        return turnstileSM;
    }

    @Test
    @DisplayName("State Building Test Turnstile3")
    void stateBuildingTest3() {
        StateMachine turnstileSM = createTurnstileSM();
        // Current state is Locked
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());

        // To transition to push the outgoing (next state) is back to itself in the Locked state.
        turnstileSM.t("push");
        Assertions.assertEquals("push", turnstileSM.currentTransition().name());
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());
        Assertions.assertEquals(LOCKED, turnstileSM.previousState());

        // Transition with coin
        turnstileSM.t("coin", "10 cents"); // 2nd parameter is optional input.
        Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

        // Transition with coin again
        turnstileSM.t("coin", "80 cents"); // 2nd parameter is optional input.
        Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

        // Transition with a push
        turnstileSM.t("push");
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());

        // Transition with a hello
        turnstileSM.t("hello");
        Assertions.assertEquals(FRED, turnstileSM.currentState());

        // Transition with a hello2
        turnstileSM.t("hello2");
        Assertions.assertEquals(FRED, turnstileSM.currentState());

        // We are stuck in the Fred state. so let's change the initial transition back to Locked state.
        turnstileSM.initial(LOCKED);
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());
        turnstileSM.t("push");
        Assertions.assertEquals(LOCKED, turnstileSM.currentState());

    }



    public static void main(String[] args){

        // A turnstile test.
        StateMachine stateMachine = createTurnstileSM();
        beginConsoleSession(stateMachine);

        // Sensor state machine
        StateMachine stateMachine2 = createSensorSM();
        beginConsoleSession(stateMachine2);

    }

}
