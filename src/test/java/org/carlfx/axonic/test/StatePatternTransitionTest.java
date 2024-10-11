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

import org.carlfx.axonic.StateMachine;
import org.carlfx.axonic.StatePattern;
import org.carlfx.axonic.tools.StateMachineCLI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.carlfx.axonic.StateEnum.STOP;
import static org.carlfx.axonic.test.TurnstileState.*;

@DisplayName("StateBuilder Test")
public class StatePatternTransitionTest {
    private static final Logger LOG = LoggerFactory.getLogger(StatePatternTransitionTest.class);
    private static StatePattern createTestStatePattern() {
        return new StatePattern()
            .initial(LOCKED)
            .t("push", LOCKED)
            .t("coin", UNLOCKED)
            .t("coin", UNLOCKED)
            .s(FRED)
            .t("coin", UNLOCKED)
            .t("boo1")
            .t("boo")
            .s(LOCKED)
            .t("hello", UNLOCKED)
            .s(FRED)
            .t("goodbye")
            .s(LOCKED)
            .t("die", FRED, STOP)
            .t("throw")
            .t("swim")
            .s(UNLOCKED)
            .t("balloon", STOP)
            .s(STOP);
    }
    @Test
    @DisplayName("A state pattern using simple versus advanced transitions to z")
    void stateBuildingTest3() {
        StateMachine turnstileSM = StateMachine.create("Turnstile", createTestStatePattern());
        String expected = """
                name: initial from: INITIAL to: LOCKED
                name: push from: LOCKED to: LOCKED
                name: coin from: LOCKED to: UNLOCKED
                name: coin from: UNLOCKED to: UNLOCKED
                name: coin from: FRED to: UNLOCKED
                name: boo1 from: UNLOCKED to: UNLOCKED
                name: boo from: UNLOCKED to: LOCKED
                name: hello from: LOCKED to: UNLOCKED
                name: goodbye from: FRED to: LOCKED
                name: die from: FRED to: STOP
                name: throw from: FRED to: FRED
                name: swim from: FRED to: UNLOCKED
                name: balloon from: UNLOCKED to: STOP
                """;
        StringBuilder sb = new StringBuilder();
        turnstileSM.getStatePattern().transitions().forEach(transition -> {
            sb.append("name: %s from: %s to: %s\n".formatted(transition.name(), transition.fromState(), transition.toState()));
        });
        System.out.println(sb);
        Assertions.assertEquals(expected, sb.toString(), "error transitions don't match.");
    }

    public static void main(String[] args){
        StateMachine turnstileSM = StateMachine.create("Turnstile", createTestStatePattern());
        StateMachineCLI.beginConsoleSession(turnstileSM);
    }
}
