package org.carlfx.axonic.test;

import org.carlfx.axonic.State;
import org.carlfx.axonic.StateMachine;
import org.carlfx.axonic.StatePattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Scanner;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.STOP;
import static org.carlfx.axonic.test.StateBuilderTest.SensorState.*;
import static org.carlfx.axonic.test.TurnStyleState.LOCKED;
import static org.carlfx.axonic.test.TurnStyleState.UNLOCKED;

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

        // test 1. see current state info
        debugInfo(turnStyleSM);

        // test 2. transition with push
        turnStyleSM.t("push");
        debugInfo(turnStyleSM);

        // test 3. transition with coin
        turnStyleSM.t("coin");
        debugInfo(turnStyleSM);

        // test 4. transition with coin
        turnStyleSM.t("coin");
        debugInfo(turnStyleSM);

        // test 5. transition with push
        turnStyleSM.t("push");
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


        String template = """
                @startuml
                %s@enduml
                """;
        StringBuilder sb = new StringBuilder();
        String pairString = "%s --> %s : %s\n";
        turnStyleSM2.getStatePattern().transitions().forEach(transition -> {
            String fromS = transition.fromState() == INITIAL ? "[*]" : transition.fromState().getName();
            String toS = transition.toState() == STOP ? "[*]" : transition.toState().getName();
            sb.append(pairString.formatted(fromS, toS, transition.name()));
        });
        System.out.println(template.formatted(sb.toString()));
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
        StringBuilder sb = new StringBuilder();
        /*
        @startuml
        [*] --> Opened : initial
        Opened --> Closing: close
        Closing --> Opening : open
        Opening --> Closing : close
        Closing --> Closed : sensor closed
        Closed --> Opening : open
        Opening --> Opened : sensor opened
        @enduml
        */
        String template = """
                @startuml
                %s@enduml
                """;
        String pairString = "%s --> %s : %s\n";
        sensorSM.getStatePattern().transitions().forEach(transition -> {
            String fromS = transition.fromState() == INITIAL ? "[*]" : transition.fromState().getName();
            String toS = transition.toState() == STOP ? "[*]" : transition.toState().getName();
            sb.append(pairString.formatted(fromS, toS, transition.name()));
        });
        System.out.println(template.formatted(sb.toString()));
    }
    private void debugInfo(StateMachine turnStyleSM) {
        LOG.info(" Chose transition: " + turnStyleSM.currentTransition());
        LOG.info("    Current state: " + turnStyleSM.currentState());
        LOG.info("       Prev state: " + turnStyleSM.previousState());
        LOG.info("Avail transitions: " + turnStyleSM.outgoingTransitions());
        LOG.info("-----------------------------------------------------");
    }
    public static void main(String[] args){
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

        turnStyleSM
                .when(LOCKED, () -> System.out.println("Secured Can not enter."))
                .when(UNLOCKED, () -> {
                    if (turnStyleSM.previousState() == LOCKED) System.out.println("You may enter");
                    if (turnStyleSM.previousState() == UNLOCKED) System.out.println("Thank you for more money!");
                });
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which event do you want to fire?");
        turnStyleSM.outgoingTransitions().forEach(transitionName -> {
            System.out.println(transitionName);
        });
        System.out.println("Press [q] to quit tutorial.");
        while (true) {
            String input = scanner.nextLine();
            // Validate input
            // go back a step before bad transition.
            turnStyleSM.tOrElse(input,
                    ()-> System.out.println("Invalid choices, try again."));

            System.out.println("input = " + input.trim());
            System.out.println("Turnstile state : " + turnStyleSM.currentState().getName());
            // quit
            if (input.trim().equalsIgnoreCase("q")) {
                System.out.println("input = " + input.trim());
                System.out.println("Bye!");
                System.exit(0);
            }
            // prompt user options
            System.out.println("Which event do you want to fire?");
            turnStyleSM.outgoingTransitions().forEach(transitionName -> {
                System.out.println(transitionName);
            });
        }
    }

}
