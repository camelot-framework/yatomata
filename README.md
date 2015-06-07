# Yatomata
[![release](http://github-release-version.herokuapp.com/github/camelot-framework/yatomata/release.svg?style=flat)](https://github.com/camelot-framework/yatomata/releases/latest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.yandex.qatools/yatomata/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.yandex.qatools/yatomata) [![SonarQube](https://img.shields.io/sonar/http/sonar.qatools.ru/ru.yandex.qatools:yatomata:master/coverage.svg?style=flat)](http://sonar.qatools.ru/dashboard/index/556)
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
        <version>1.6</version>
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
    Yatomata<MyFSM> fsm = new FSMBuilder(MyFSM.class).build();

    fsm.getCurrentState();   // returns instance of Stopped
    fsm.isStopped();         // returns false
    fsm.fire(new Run());     // returns instance of Running
    fsm.fire(new Stop());    // returns instance of Stopped
    fsm.getFSM();            // returns instance of MyFSM
    fsm.isStopped();         // returns true

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

### Custom instantiation method for each state

You can define the methods with `@NewState` annotation that can be used as state initializers for your states depending
on the types of the incoming messages. Each method should have two arguments: class of the new state and the incoming event.
FSM class must have only single @NewState method with the only argument, which will be used to initialize the initial state.
Example:

```java
    @FSM(start = Undefined.class)
    @Transitions({
            @Transit(from = Undefined.class, to = Started.class, on = Start.class),
            @Transit(from = Started.class, to = Stopped.class, on = Stop.class),
    })
    public class MyFSM {

        @NewState
        public Started initState(Class<Started> stateClass, Start event) {
            return new Started();
        }

        @NewState
        public Stopped initState(Class<Stopped> stateClass, Stop event) {
            return new Stopped();
        }

        @NewState
        public Undefined initState(Class<Undefined> stateClass) {
            return new Undefined();
        }
    }
```

### Superclasses annotations

You can implement your own class hierarchy according to your preference. All the annotated methods and class annotations
within superclasses will be inherited by their derived classes.

### Exception handling

Yatomata allows you to implement the methods annotated with `@OnException` annotation. Such methods will be used as the
exception handlers for your FSM. This ability allows you to skip the try-catch blocks declaration within every transition
hook method.
Example:

```java
    @FSM(start = Quotient.class)
    @Transitions({
            @Transit(on = Denominator.class),
    })
    public class MyFSM {

        @OnTransit
        public void divide(Quotient quotient, Denominator denominator) {
            quotient.setValue(quotient.getValue() / denominator.getValue());
        }

        @OnException(preserve = true)
        public void onArithmeticException(ArithmeticException e, Quotient from, Quotient to, Denominator den) {
            logger.info("Failed to perform the division", e);
        }
    }
```
The `preserve` attribute (defaults to false) indicates if the transition still must be performed even if the exception
is thrown.

**Important!**  All the declared `@OnException` methods, whose signature matches the occurred exception, will be called
when exception is thrown. Thus if you declare the method accepting the `Throwable` as an argument, it will be called
upon each occurring error.

### StopConditionAware interface

If you want to stop your FSM by a custom condition, you can implement the `StopConditionAware` interface with your class and
then implement the `isStopRequired` method, which accepts a new state object and an incoming event.

```java
    @FSM(start = Idle.class)
    @Transitions({
            @Transit(from = Idle.class, on = Run.class, to = Running.class),
            @Transit(from = Running.class, on = Stop.class, to = Stopped.class, stop = true),
    })
    public class MyFSM implements StopConditionAware<State, Event> {

        boolean isStopRequired(State state, Event event){
            return event instanceof Stop;
        }
    }
```

**Important!** `isStopRequired` cannot override the true stop condition which is defined by a transition. Thus if your transition
forces FSM to stop, this condition will be ignored.

## FSM implementation examples (see also the [tests](https://github.com/yandex-qatools/yatomata/tree/master/src/test/java/ru/yandex/qatools/fsm))

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

