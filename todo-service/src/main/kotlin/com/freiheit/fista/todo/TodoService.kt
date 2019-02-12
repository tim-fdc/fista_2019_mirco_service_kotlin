package com.freiheit.fista.todo

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
val todoService = TodoService(store = TodoStore, httpClient = httpClientFactory(), config = TodoService.Config.fromEnv())

/**
 * This service contains the business logic to create, update etc. a [Todo].
 */
class TodoService(
    private val store: IStore<Todo>,
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
            fun fromEnv(): Config = Config(userServiceUrl = userServiceUrl())
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

    open class NotFoundException(override val message: String?) : Exception(message)
    class UserNotFoundException(message: String?) : NotFoundException(message)
    class TodoNotFoundException(message: String?) : NotFoundException(message)

    /**
     * Use dedicated dispatcher when accessing external resources through IO. It can happen very easily that calls
     * to external resources block, which can also block your dispatcher handling incoming requests.
     */
    private val userServiceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    suspend fun create(assignee: String, description: String): Todo.Active {
        verifyUser(assignee)
        val todo = OpenTodo(
            id = generateId(),
            data = Todo.Data(
                assignee = UserId(assignee),
                description = description
            )
        )
        return todo.also { store.save(it.id, it) }
    }

    private suspend fun verifyUser(assignee: String) {
        // TODO: EXERCISE 1: TASK 3
        return
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

    fun assign(id: String, assignee: String): Todo.Active {
        val todo = loadTodo(id)
        return when (todo) {
            is Todo.Active -> todo.assign(assignee)
            is Todo.Closed -> throw IllegalStateException("Cannot reassign a closed Todo.")
        }.also { store.save(it.id, it) }
    }

    fun markCompleted(id: String): CompletedTodo {
        return loadTodo(id)
            .complete()
            .also { store.save(it.id, it) }
    }

    private fun loadTodo(id: String) = store.load(id) ?: throw TodoNotFoundException("Todo [$id] does not exist.")

    private fun Todo.Active.assign(assignee: String) = when (this) {
        is OpenTodo -> OpenTodo(
            id = id,
            data = data.copy(assignee = UserId(assignee))
        )
        is InProgressTodo -> InProgressTodo(
            id = id,
            data = data.copy(assignee = UserId(assignee)),
            startedAt = startedAt
        )
    }

    private fun Todo.complete() = when (this) {
        is OpenTodo -> CompletedTodo(
            id = id,
            data = data,
            startedAt = System.currentTimeMillis()
        )
        is InProgressTodo -> CompletedTodo(
            id = id,
            data = data,
            startedAt = startedAt
        )
        is CompletedTodo -> this
    }

    private fun generateId() = "${UUID.randomUUID()}"
}
