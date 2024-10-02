package org.carlfx.axonic;

import java.util.List;

public interface FSM {
    FSM t(String transition);
    Transition currentTransition();
    List<String> outgoingTransitions();
    State currentState();
    State previousState();
    FSM when(State state, Runnable codeBlock);
}
