package org.carlfx.axonic;

import java.util.List;
import java.util.Set;

public interface FSMBuilder {
    FSMBuilder t(String transition);
    FSMBuilder t(Transition transition);
    FSMBuilder s(State state);
    List<Transition> transitions();
    Set<State> states();
    State currentState();
    FSMBuilder initial(State state);
    FSMBuilder stop();
}
