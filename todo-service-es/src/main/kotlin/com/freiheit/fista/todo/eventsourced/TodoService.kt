package com.freiheit.fista.todo.eventsourced

import com.freiheit.fista.todo.eventsourced.model.Command
import com.freiheit.fista.todo.eventsourced.model.Todo
import com.freiheit.fista.todo.eventsourced.model.UserId
import com.freiheit.fista.todo.eventsourced.persistence.CommandQueue
import com.freiheit.fista.todo.eventsourced.persistence.IStore
import com.freiheit.fista.todo.eventsourced.persistence.TodoStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.isSuccess
import io.ktor.util.cio.toByteArray
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Global and singleton instance of the [TodoService].
 */
val todoService = TodoService(
    store = TodoStore,
    httpClient = httpClientFactory(),
    config = TodoService.Config.fromEnv()
)

/**
 * This service contains the business logic to create, update etc. a [Todo].
 */
class TodoService(
    private val store: IStore<Todo.Created, Todo.Created>,
    private val httpClient: HttpClient,
    private val config: Config
) {

    /**
     * Put configurations of services into a dedicated config class that is close to the service. It avoids global
     * configurations which may get a mess quite easily.
     */
    class Config(val userServiceUrl: String) {
        /**
         * The URL of the external user service. A micro service should always get its configuration from its environment.
         *
         * Since calling [System] is a JVM platform call it may fail. Hence, "!!" is
         * used to throw if the system returns null.
         */
        companion object {
            fun userServiceUrl() = System.getProperty("USER_SERVICE_URL", "http://localhost:8081/")!!
            fun fromEnv(): Config =
                Config(userServiceUrl = userServiceUrl())
        }
    }

    /**
     * Models the user as returned from the user-service.
     */
    data class User(val id: String, val email: String)

    /**
     * Models a GSON list type containing users.
     */
    private val userListType = object : TypeToken<List<User>>() {}.type!!

    /**
     * Wraps an [HttpResponse] into an Exception.
     */
    class HttpClientException(response: HttpResponse) : IOException("HTTP Error ${response.status}")

    class UserNotFoundException(override val message: String?) : Exception(message)

    /**
     * Use dedicated dispatcher when accessing external resources through IO. It can happen very easily that calls
     * to external resources block, which can also block your dispatcher handling incoming requests.
     */
    private val userServiceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    suspend fun create(assignee: String, description: String): String {
        verifyUser(assignee)
        return Command(
            todoId = generateId(),
            payload = Command.Payload.CreateTodo(
                assignee = UserId(assignee),
                description = description
            )
        ).also { CommandQueue.send(it) }.todoId
    }

    private suspend fun verifyUser(assignee: String) {
        //*******
        //EXERCISE 1
        //*******
        return

        //*******
        //SOLUTION EXERCISE 1
        //*******
//        if (!httpClient.getAllUsers().any { it.id == assignee })
//            throw UserNotFoundException("User $assignee does not exist.")
    }

    /**
     * This function extends [HttpClient] by a function to get all users from the user service.
     */
    private suspend fun HttpClient.getAllUsers(): List<User> {
        //Calling external resources may easily block in particular when using blocking IO. Therefore, a dedicated
        //dispatcher is used to process HTTP requests
        return withContext(userServiceDispatcher) {
            try {
                //performs the call
                val call = call {
                    url("${config.userServiceUrl}/users/")
                    method = Get
                }

                // if the call is not successful throw an exception
                if (!call.response.status.isSuccess()) {
                    throw HttpClientException(call.response)
                }

                // otherwise take the returned json and create a list of users.
                val json = String(call.response.content.toByteArray())
                return@withContext Gson().fromJson<List<User>>(json, userListType)
            } catch (e: Exception) {
                throw e
            }
        }
    }
    fun assign(id: String, assignee: String) {
        Command(
            todoId = id,
            payload = Command.Payload.AssignTodo(
                assignee = UserId(assignee)
            )
        ).also { CommandQueue.send(it) }
    }

    fun markCompleted(id: String) {
        Command(
            todoId = id,
            payload = Command.Payload.MarkCompleted
        ).also { CommandQueue.send(it) }
    }

    fun getTodo(id: String): Todo.Created? {
        return store.load(id)
    }

    private fun generateId() = "${UUID.randomUUID()}"
}
