Yatomata
=============

Yet Another auTomata (finite state machine implementation in java).

There are a lot of FSM implementations, but typically they are very sophisticated and it's hard to incorporate them in your project.
Yatomata tries to be very simple and nice. It follows the declarative way and uses the convention over configuration paradigm.

## User Guide

### Setup

Just add the following dependency to your pom.xml:

```xml
    <dependency>
        <groupId>ru.yandex.qatools</groupId>
        <artifactId>yatomata</artifactId>
        <version>1.1</version>
    </dependency>
```

### Basics

First create the FSM class:
```java
    @FSM(start = Stopped.class)
    @Transitions({
            @Transit(from = Stopped.class, on = Run.class, to = Running.class),
            @Transit(from = Running.class, on = Stop.class, to = Stopped.class, stop = true),
    })
    public class MyFSM {

    }
```
It should have the `@FSM` annotation defining the initial (start) state class.
It also should have the `@Transitions` definition, containing the list of the available transitions.
Each transition should define the `from` state(s), the `to` state and the list of the events in `on`.
Each transition may also have the `stop` value, defining the final transition (the end state).

* The `from`, `to` and `stop` attributes are optional, while `on` is a mandatory attribute.
* If no `from` is present, then it would be considered as a current state.
* If no `to` is specified, then there would be no state changing (the state would remain the same as before).

FSM usage example follows:

```java
    Engine<MyFSM> engine = new Engine(MyFSM.class, new MyFSM());

    engine.getCurrentState();   // returns instance of Stopped
    engine.isStopped();         // returns false
    engine.fire(new Run());     // returns instance of Running
    engine.fire(new Stop());    // returns instance of Stopped
    engine.isStopped();         // returns true

```

### Custom `init` method for each state

You can define the `initStateMethod` with the signature according to your state and event classes.  This method
should be unique within the class and it will be used during the instantiation of the new state object. Example:

```java
    @FSM(start = Undefined.class, initStateMethod = "initState")
    @Transitions({
            @Transit(from = Undefined.class, to = Started.class, on = Start.class),
    })
    public class MyFSM {

        public State initState(Class<? extends State> stateClass, Event event) throws IllegalAccessException, InstantiationException {
            State res = stateClass.newInstance();
            res.setEvent(event);
            return res;
        }
    }
```

### Hook methods

You can declare the three types of the hook methods within the FSM class.

The main hook method should have the `@OnTransit` annotation and will be called during any of the transitions.
It may have the three options of the arguments:
* Single argument: incoming event
* Two arguments: current state/next state, incoming event
* Three arguments: current state, next state, incoming event

Each transition initiates the call of the single hook method which meets the signature of the transition.

**Important!** If there are several hook methods with `@OnTransit` annotation, that meet the transition signature, only the first of them (which is found first) will be called.

Besides the `@OnTransit` annotation you can declare the `@BeforeTransit` and `@AfterTransit` methods.
The main difference from the main transit methods is that these methods will be invoked before and after the transition accordingly.
Moreover all of the methods annotated with these annotations that meet the transition arguments, will be called one time before and
one time after the transition. This means that you can declare the several `@BeforeTransit` methods with the different signatures and if
all of them match the transition, they will be invoked.

Example:

```java
    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = Run.class, to = Running.class),
            @Transit(from = Running.class, on = Stop.class, to = Stopped.class, stop = true),
    })
    public class MyFSM {
        @OnTransit
        public void onRun(Idle from, Running to, Run event){}

        @OnTransit
        public void onRun(Idle from, Run event){}

        @BeforeTransit
        public void onBeforeRun(Idle state, Run event){}

        @BeforeTransit
        public void onBeforeRun(Run event){}
    }
```

In the example above when `Run` event is caught, there will be the call to the both of the `onBeforeRun` methods.
But only the first `onRun` method will be invoked.

## FSM implementation examples (see also the [tests](https://github.yandex-team.ru/qafw/biosm/tree/master/src/test/java/ru/yandex/qatools/fsm))

### Execute state machine

```java
    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = ProcessStarted.class, to = Running.class),
            @Transit(from = Idle.class, on = TerminateProcess.class, to = Cancelling.class),
            @Transit(from = {Idle.class, Cancelling.class}, on = ProcessTerminated.class, stop = true),
            @Transit(from = Cancelling.class, on = ProcessStarted.class, stop = true),
            @Transit(from = Running.class, on = {ProcessCompleted.class, ProcessFailed.class, ProcessTerminated.class}, stop = true),
            @Transit(from = Running.class, on = TerminateProcess.class)
    })
    public class ExecuteStateMachine {
        @OnTransit
        public void onProcessStarted(Idle from, Running to, ProcessStarted event){}

        @OnTransit
        public void onProcessTerminate(Running from, TerminateProcess event){}

        @OnTransit
        public void onProcessStartedAtCancelling(Cancelling from, ProcessStarted event){}

        @OnTransit
        public void onProcessTerminatedAtCancelling(ExecuteState from, ProcessTerminated event){}
    }
```

### Single state FSM (infinite loop)

```java
    @FSM(start = CompletedCounterState.class)
    @Transitions( @Transit(on = {TestCompletionInfo.class}) )
    public class CompletedCounterStateMachine {
        @OnTransit
        public void aggregate(CompletedCounterState state, TestCompletionInfo event){}
    }
```

