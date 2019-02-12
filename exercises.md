# Exercises

Below you will find exercises. All of them have tasks. Some of them are MUST and some are SHOULD. The idea of this workshop 
and its exercises is to enable you to work with this project. It is therefore totally OK if you just do the MUSTs and 
later tackle the SHOULDs.
 
## Exercise 1 
### Task 1 (MUST) 
1. Find somebody to pair with
2. Setup your environment
 * Import the project into your Idea
 * Try to run `./gradlew user-service:formatKotlin user-service:clean user-service:build` and 
 `./gradlew todo-service:formatKotlin todo-service:clean todo-service:build` 

### Task 2 (MUST)
The customer is already able to create and assign a Todo. The product owner requests the possibility to delete a todo.
The API (see `Main.kt`) already contains an endpoint to create a Todo. You will also find a `TODO: EXERCISE 1: TASK 2` to implement
this new feature. 

Take the ID from the call and pass it to the `TodoService`.

### Task 3 (SHOULD)
The customer is already able to create, assign and delete a Todo. The product owner requests the possibility to complete a todo.
The API (see `Main.kt`) already contains an endpoint to create a Todo. You will also find a `TODO: EXERCISE 1 TASK 3` to implement
this new feature. Class `SetStatusRequestBody` (see `Main.kt`) already models the according API.

Take the ID from the call and pass it to the `TodoService`.

## Exercise 2
### Task 1 (MUST)
In the last sprint the user service has been deployed, which contains an interface to get all users. Although this 
endpoint is not yet final, your product owner (PO) decided to connect this external interface already. 
He wants to get in touch with the external team and start alignments as early as possible. What could be 
better to achieve this goal if the teams get technically connected.

The Todo service already creates Todos (see `TodoService#create` and `Main` lines 92ff), but does not yet verify 
if the user really exists. Your exercise is to call the external service and make sure 
the user exists. The TodoService already has a method to call the external user service (`suspend fun HttpClient.getAllUsers(): List<User>`). 
Of course this code is tested, but the tests are currently failing (simply run `./gradlew todo-service:test` to see them failing). 
What's missing is to call the external service and make sure the assignee is a known user.

### Task 2 (MUST)
Your product owner asks you if it would be technically OK to still create the Todo although the check to verify the user times out. 
Of course this is possible - it is software. After a while of reading and listening you decided to call the 
`suspend function TodoService#verifyUser` with a timeout and catch the cancellation.

Go ahead and use the `withTimeout` function and catch any `CancellationException`. If the exception gets caught, 
simply return true.  

How would you test this?
 
### Task 3 (SHOULD)
You listened again to a talk and learned that Kotlin also has supervision, which basically prevents parent coroutines to 
fail if a child fails.

Implement a `supervisorScope` and launch the `verifyUser` function in it. Make sure to pass a CoroutineExceptionHandler 
into the launch to still log any exception that gets thrown.

How would you test this?

Happy Hakking!
