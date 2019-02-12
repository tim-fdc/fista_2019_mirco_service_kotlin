package com.freiheit.fista.user.api

import com.freiheit.fista.user.UserService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext

/**
 * REST API to access the users.
 */
class UsersApiHttp(private val userService: UserService) {

    /**
     * Routing is a tree data structure with "/" -> route -> route -> route -> {get, post, put, delete}.
     * Root node is mounted under "/".
     */
    fun createRoutes(routing: Routing) =
            routing {
                route("users") {
                    get {
                        call.respond(userService.getUsers())
                    }
                    get("/{user-id}/") {
                        val userId = userIdFromCall()
                        call.respond(userService.getUser(userId))
                    }
                    route("subscriptions") {
                        get("/{user-id}/") {
                            val userId = userIdFromCall()
                            call.respond(userService.getSubscriptions(userId))
                        }
                    }
                }
            }

    private fun PipelineContext<Unit, ApplicationCall>.userIdFromCall() =
            (call.parameters["user-id"]
                    ?: throw throw IllegalArgumentException("user-id missing."))
}
