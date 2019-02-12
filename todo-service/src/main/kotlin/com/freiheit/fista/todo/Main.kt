package com.freiheit.fista.todo

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * Creates an embedded Netty server and installs Ktor modules (https://ktor.io/).
 */
fun main() {
    val port = System.getProperty("TODO_SERVICE_PORT", "8082").toInt()

    embeddedServer(Netty, port) { installModule() }.start(wait = true)
}

/**
 * This data class models the request to create a new Todo.
 * @param assignee Any value can be set for the assignee.
 * @param description The description of the todo.
 */
data class CreateTodoRequestBody(val assignee: String, val description: String)
data class SetStatusRequestBody(val status: Status)
enum class Status { COMPLETED }

/**
 * Setup the KTOR server.
 * Ktor is an un-opinionated framework to build asynchronous servers and clients (https://ktor.io/).
 * Modules are installed to an application.
 */
fun Application.installModule() {
    // install JSON (GSON) support
    install(ContentNegotiation) {
        gson { setPrettyPrinting() }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { e ->
            call.respondText(text = e.message ?: "", status = HttpStatusCode.BadRequest)
        }

        exception<TodoService.NotFoundException> { e ->
            call.respondText(text = e.message ?: "", status = HttpStatusCode.NotFound)
        }

        exception<RuntimeException> {
            // do not expose internal information of a RuntimeException
            call.respondText("", status = HttpStatusCode.InternalServerError)
        }
    }

    /**
     * To structure page request handling, KTOR has a routing component (https://ktor.io/servers/features/routing.html).
     *
     * Routing is a tree data structure with "/" -> route -> route -> route -> {get, post, put, delete}.
     * Root node is mounted under "/".
     */
    routing {
        route("todos") {
            post("/") {
                val body: CreateTodoRequestBody = call.getRequestBody()
                call.respond(todoService.create(body.assignee, body.description))
            }

            get("/{id}") {
                //TODO: implement returning a ToDo.
            }

            put("/{id}/assignee/{user-id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("id missing.")
                val assignee = call.parameters["user-id"] ?: throw IllegalArgumentException("user-id missing.")
                call.respond(todoService.assign(id, assignee))
            }

            put("/{id}/status") {
                // TODO: EXERCISE 1: TASK 3
            }

            delete("/{id}") {
                // TODO: EXERCISE 1: TASK 2
            }
        }

        get("/health") {
            //TODO: proper implementation missing. See user-service for inspiration.
            call.respondText { "OK" }
        }
    }
}

/**
 * Gets the body from the call and creates an instance of the corresponding type.
 */
private suspend inline fun <reified V : Any> ApplicationCall.getRequestBody(): V = receiveOrNull()
        ?: throw IllegalArgumentException("Request body is invalid.")

/**
 * Creates an HTTP client. The timeouts are rather relaxed.
 */
fun httpClientFactory() = HttpClient(engineFactory = Apache) {
    engine {
        followRedirects = true
        socketTimeout = 5000
        connectTimeout = 5000
        connectionRequestTimeout = 3
        // try to always limit resources in particular threads. Having knowledge about the number of threads
        // used is crucial. The creation of more and more threads may quickly cause consume too much memory.
        threadsCount = 1
    }
}
