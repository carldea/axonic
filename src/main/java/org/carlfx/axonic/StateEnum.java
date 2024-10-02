package org.carlfx.axonic;

public enum StateEnum implements State {
    INITIAL("Initial"),
    STOP("Stop"),
    INVALID("Invalid");

    final String name;

    StateEnum(String name){
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}
