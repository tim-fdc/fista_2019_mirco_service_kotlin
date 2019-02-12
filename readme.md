# Why this project?
This project is used for trainings and workshops and moreover is is a take away! Something that 
can be used to work on after the workshop.

It contains two independent Kotlin projects. One project implements a user-service, which 
is the external backend service. The other project implements a todo-service. T

The user-service has a GRPC and a REST interface (for demonstration purposes). Both services 
use [Ktor](http://ktor.io). In addition, the user-service uses a custom implementation to link 
coroutines with GRPC channels.


The todo-service is the service, in which exercises take place. This service calls the user-service. Internally
the todo-service has two implementations. One is a synchronous service. The other facilitates an event sourcing 
approach.

Summarizing, this project demonstrates the usage of Kotlin, Coroutines and Event Sourcing. And since
it has two little services communicating with each other, it is also a micro-service project.

# Who is the audience?
It has been built for [FISTA 2019](https://fista.iscte-iul.pt/). So, all students attending the workshop
are the primary audience.

# Build and run the project
The project is build using Gradle. 

Build and run the user-service
`./gradlew user-service:formatKotlin user-service:clean user-service:build`

Build and run the todo-service
`./gradlew todo-service:formatKotlin todo-service:clean todo-service:build`

There are also some helper scripts `build-and-run-todo-service.sh` and `build-and-run-todo-service.sh`.


# Exercises
The exercises can be found in file [exercises.md]().