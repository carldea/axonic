package org.carlfx.axonic;

import java.util.*;
import java.util.function.Consumer;

import static org.carlfx.axonic.StateEnum.INITIAL;
import static org.carlfx.axonic.StateEnum.INVALID;

public class StateMachine implements FSM {
    private StatePattern statePattern;
    protected State previousState;
    protected State currentState;
    protected Transition currentTransition;

    private Map<State, List<Runnable>> stateCodeMap = new LinkedHashMap<>();

    private StateMachine(StatePattern statePattern) {
        this.statePattern = statePattern;
    }
    public StatePattern getStatePattern() {
        return statePattern;
    }
    public static StateMachine create(StatePattern statePattern) {
        StateMachine stateMachine = new StateMachine(statePattern);
        stateMachine.previousState = INITIAL;
        stateMachine.currentTransition = statePattern.lookupOutgoingTransitions(INITIAL).getFirst();
        if (stateMachine.currentTransition == null) {
            throw new RuntimeException("StatePattern does not contain an initial transition");
        }
        stateMachine.currentState = stateMachine.currentTransition.toState();
        return stateMachine;
    }
    public static StateMachine create(Consumer<StatePattern> statePatternConsumer) {
        StatePattern statePattern = new StatePattern();
        statePatternConsumer.accept(statePattern);
        return create(statePattern);
    }

    @Override
    public FSM t(String transition) {
        List<Transition> outTransitions = statePattern.lookupOutgoingTransitions(currentState());
        Optional<Transition> choice = outTransitions.stream().filter(t -> t.name().equals(transition)).findFirst();
        // current state find out transition if make previous = current and transition to as next state.
        choice.ifPresentOrElse( t -> {
                // transition to next state.
                previousState = currentState();
                currentState = t.toState();
                currentTransition = t;
                // TODO invoke all when state functions
                List<Runnable> runnables = stateCodeMap.get(t.toState());
                if (runnables != null) {
                    runnables.forEach(Runnable::run);
                }
            },
                ()-> currentState = INVALID); // don't set previous so caller can recover.
        // if (previous != current) call when (code blocks)
        return this;
    }

    @Override
    public Transition currentTransition() {
        return currentTransition;
    }

    @Override
    public List<String> outgoingTransitions() {
        List<Transition> outTransitions = statePattern.lookupOutgoingTransitions(currentState());
        return outTransitions.stream().map(t -> t.name()).toList();
    }

    @Override
    public State currentState() {
        return currentState;
    }

    @Override
    public State previousState() {
        return previousState;
    }

    @Override
    public FSM when(State state, Runnable codeBlock) {
        List<Runnable> list = stateCodeMap.get(state);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(codeBlock);
        stateCodeMap.put(state, list);
        return this;
    }

    public void tOrElse(String transition, Runnable invalid) {
        if (outgoingTransitions().contains(transition)) {
            // valid outgoing transitions
            t(transition);
        } else {
            invalid.run();
        }
    }
}
