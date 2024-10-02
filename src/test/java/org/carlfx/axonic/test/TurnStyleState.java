package org.carlfx.axonic.test;

import org.carlfx.axonic.State;

public enum TurnStyleState implements State {
    LOCKED("Locked"),
    UNLOCKED("Unlocked");

    final String name;

    TurnStyleState(String name){
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}
