Please view the Wiki [here](https://github.com/carldea/axonic/wiki)
# What's new? [Release notes](https://github.com/carldea/axonic/releases)
- [0.0.1](https://github.com/carldea/axonic/releases/tag/release%2F0.0.1) 10/2024 - Initial creation.
- [0.0.2](https://github.com/carldea/axonic/releases/tag/release%2F0.0.2) 10/06/2024 - Added a CLI tool to help developer visualize diagrams and transition tables.

# Axonic
A small Java based state machine.


## Quick Start
To use Axonic in your project, download and install Java 21+ JDK.

*Gradle:*
```gradle
implementation 'org.carlfx:axonic:0.0.2'
```

*Maven:*
```xml
<dependency>
    <groupId>org.carlfx</groupId>
    <artifactId>axonic</artifactId>
    <version>0.0.2</version>
</dependency>
```

Project using Java Modules (JPMS) will want to do the following in the consuming module:
```java

requires org.carlfx.axonic;

```

# Demo
Below you should see an example of a turn style with an additional state 'Fred'.

![Screenshot 2024-10-06 at 10 55 33 PM](https://github.com/user-attachments/assets/c39371ab-9c55-4cea-bdc7-ea6f58257e84)

Let's begin with creating the states of a turnstile. We will also add the Fred state.
**Note:** The axonic State is an interface with a `getName()` method allowing Classes, Records and Enums to be used as states.

```java
// Enum representing all states
import org.carlfx.axonic.State;

public enum TurnstileState implements State {
    LOCKED("Locked"),
    UNLOCKED("Unlocked"),
    FRED("Fred");

    final String name;

    TurnstileState(String name){
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}
```

## Predefined States
Predefined states are available to indicate an `INITIAL`, `STOP`, `INVALID` states located in

```java
public enum StateEnum implements State {
    /**
     * Initial state
     */
    INITIAL("Initial"),
    /**
     * Stop state
     */
    STOP("Stop"),
    /**
     * Invalid state
     */
    INVALID("Invalid");

  // ... the rest
}
```
Next, we'll use the factory method to create a StateMachine based on a name and a state pattern. A state pattern is an object that represents the state machines states and transitions.

```java
// Creating a turnstile State Machine based on a state pattern.
StateMachine turnstileSM = StateMachine.create("Turnstile", statePattern ->
   statePattern.initial(LOCKED)
               .t("push")
               .t("coin")
               .s(UNLOCKED)
               .t("coin")
               .t("push")
               .s(LOCKED)
               .t("hello")
               .s(FRED)
               .t("hello2"));
```

Next, you can invoke code when a state is encountered.

```java
turnstileSM.when(LOCKED, (t, input) -> 
              System.out.println("Secured. You may not enter. Transition %s from state %s, input=%s".formatted(t.name(), t.fromState(), input)))
           .when(UNLOCKED, () -> {
              if (turnstileSM.previousState() == LOCKED) System.out.println("You may enter");
              if (turnstileSM.previousState() == UNLOCKED) System.out.println("Thank you for more money!");
            });
```

As you will notice there are two ways to invoke code blocks:

1. `BiConsumer<Transition, Object>` - `t` (Transition object) and an optional `input` parameter passed in from a previous transition. A Transition object has a name, from state and to state.
2. `Runnable` - This allows the developer to execute any arbitrary code.

Now that you've defined the State Machine let's start interacting with it.
# Testing your state machine

```java
// Current state is Locked
Assertions.assertEquals(LOCKED, turnstileSM.currentState());

// To transition to push the outgoing (next state) is back to itself in the Locked state.
turnstileSM.t("push");
Assertions.assertEquals("push", turnstileSM.currentTransition().name());
Assertions.assertEquals(LOCKED, turnstileSM.currentState());
Assertions.assertEquals(LOCKED, turnstileSM.previousState());

// Transition with coin
turnstileSM.t("coin", "c 3"); // 2nd parameter is optional input.
Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

// Transition with coin again
turnstileSM.t("coin");
Assertions.assertEquals(UNLOCKED, turnstileSM.currentState());

// Transition with a push
turnstileSM.t("press");
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

```

The output is the following:

```
Secured. You may not enter. Transition push from state LOCKED, input=push
You may enter
Thank you for more money!
Secured. You may not enter. Transition push from state UNLOCKED, input=push
```

# How to diagram your state pattern
Axionic currently supports simple state diagrams using Mermaid and Plantuml.

## How to diagram state machine in Mermaid syntax
```java
System.out.println(DiagramHelper.toMermaid(turnstileSM));
```

Outputs the following:

```
stateDiagram-v2
   Locked --> Locked : push
   Locked --> Unlocked : coin
   Unlocked --> Unlocked : coin
   Unlocked --> Locked : push
   Locked --> Fred : hello
   Fred --> Fred : hello2
   [*] --> Locked : initial
```

Copy and paste the above to https://mermaid.live/

## How to diagram state machine in PlantUml syntax
```java
System.out.println(DiagramHelper.toPlantUml(turnstileSM));
```

Outputs the following:

```
@startuml
Locked --> Locked : push
Locked --> Unlocked : coin
Unlocked --> Unlocked : coin
Unlocked --> Locked : push
Locked --> Fred : hello
Fred --> Fred : hello2
[*] --> Locked : initial
@enduml
```

Copy and paste the above to https://www.plantuml.com/plantuml/uml


# Interactive State Machine CLI
A simple CLI can be created from the `org.carlfx.axonic.tools` package and class method `StateMachineCLI.beginConsoleSession(turnstileSM);`

Before we begin let's call the help to see the available options to help display our state machine. Afterwards we will test each transition of our turnstile example.
## Help
The help menu will describe the available commands.

```bash
Here is a state pattern of a Turnstile depicted here: 

@startuml
[*] --> Locked : initial
Locked --> Locked : push
Locked --> Unlocked : coin
Unlocked --> Unlocked : coin
Unlocked --> Locked : push
Locked --> Fred : hello
Fred --> Fred : hello2
@enduml

 NOTE: If you are in stuck state type: jump <my_state>. e.g. jump Locked
       Also to see all states type: show states
Press [h] for help.
Press [q] to quit.
   Your initial state is: Locked

Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

Enter command or transition: h

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

```
## Displaying all states
All states can be displayed with the `show states` command

```bash
Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

Enter command or transition: show states

Showing available states for Turnstile
 States: [Fred, Locked, Initial, Unlocked]

```

## Displaying a transition table
A transition table describes the state pattern (flow). First column is the state, and subsequent columns are the transitions.

```bash

Enter command or transition: transition table

State     initial  push    coin      hello  hello2  
----------------------------------------------------
Fred      X        X       X         X      Fred    
Locked    X        Locked  Unlocked  Fred   X       
Initial   Locked   X       X         X      X       
Unlocked  X        Locked  Unlocked  X      X       


```

## Diagram with Mermaid notation
Enter the `diagram mermaid` command to display the state machine in Mermaid.

```bash

Enter command or transition: diagram mermaid

----------------------------------------
Diagram mermaid https://mermaid.live/
----------------------------------------

stateDiagram-v2
   [*] --> Locked : initial
   Locked --> Locked : push
   Locked --> Unlocked : coin
   Unlocked --> Unlocked : coin
   Unlocked --> Locked : push
   Locked --> Fred : hello
   Fred --> Fred : hello2


----------------------------------------

```

When going to the [Mermaid Live](https://mermaid.live/) to display the diagram (cut and paste) it should look like the following:

![Screenshot 2024-10-07 at 12 55 13 AM](https://github.com/user-attachments/assets/65ff35ac-a444-48db-be10-4718c4cef2ee)


## Diagram with PlantUml notation
Enter the `diagram plantuml` command to display the state machine in PlantUml.

```bash

Enter command or transition: diagram plantuml

----------------------------------------
Diagram plantuml https://www.plantuml.com/plantuml/uml
----------------------------------------

@startuml
Locked --> Locked : push
Locked --> Unlocked : coin
Unlocked --> Unlocked : coin
Unlocked --> Locked : push
Locked --> Fred : hello
Fred --> Fred : hello2
[*] --> Locked : initial
@enduml

----------------------------------------

```

When going to the [PlantUml](https://www.plantuml.com/plantuml) to display the diagram (cut and paste) it should look like the following:

![Screenshot 2024-10-06 at 10 55 33 PM](https://github.com/user-attachments/assets/e350eb05-d297-4038-b56a-5eae4b303edd)


## Testing each transition
Let's begin our session to test each transition.

```bash
Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

Enter transition: 0

Secured Can not enter. called push from state LOCKED, input=push
transition: push - input = p

Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

Enter transition: 1

You may enter
transition: coin - input = c

Your current state is: Unlocked
Where to go next? (Type the transition name or line number to move to the next state)
0) coin ---> (Unlocked) 
1) push ---> (Locked) 

Enter transition: 0

Thank you for more money!
transition: coin - input = c

Your current state is: Unlocked
Where to go next? (Type the transition name or line number to move to the next state)
0) coin ---> (Unlocked) 
1) push ---> (Locked) 

Enter transition: 1

Secured Can not enter. called push from state UNLOCKED, input=push
transition: push - input = p

Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

Enter transition: 2

transition: hello - input = h

Your current state is: Fred
Where to go next? (Type the transition name or line number to move to the next state)
0) hello2 ---> (Fred) 

Enter transition: 0

transition: hello2 - input = h

Your current state is: Fred
Where to go next? (Type the transition name or line number to move to the next state)
0) hello2 ---> (Fred) 

Enter transition: jump Locked

Jumping to a new state Locked
Your initial state is: Locked

Your current state is: Locked
Where to go next? (Type the transition name or line number to move to the next state)
0) push ---> (Locked) 
1) coin ---> (Unlocked) 
2) hello ---> (Fred) 

```
Above you will notice when transitioning with `hello2` you are stuck at the `Fred` state. Here you can jump command to move the initial transition to point to any state. To programmatically perform the jump is to use the following function on the state machine.

```java
turnstileSM.initial(LOCKED);
```

You can also affect the initial transition on the StatePattern instance by calling the `moveInitial(state);` method.

Keep in mind the StatePattern defines the state machine's flow pattern and the StateMachine maintains the state when the caller is transitioning or progressing through (flow).

Happy coding and I hope this will help you with managing state.

Carl
