package com.freiheit.fista.user

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * A database mock.
 */
object UsersDb {
    private fun newId() = UUID.randomUUID().toString()
    val users = listOf(
            UserService.User(newId(), "herbie"),
            UserService.User(newId(), "jacky"),
            UserService.User(newId(), "betty")
    ).associateBy { it.id }

    val userSubscriptions = users.values.map {
        it.id to "${it.id} subscriptions"
    }.toMap()
}

/**
 * The [UserService] contains all the business logic needed to transform between the source data (usually from some
 * persistent system) and the API.
 */
class UserService {
    data class User(val id: String, val email: String)
    class UserNotFoundException(override val message: String) : Exception()

    suspend fun getUsers(): Collection<User> {
        // simulate database access...
        delay((100..1000).random().toLong())
        return UsersDb.users.values
    }

    suspend fun getSubscriptions(userId: String): String {
        // simulate database access...
        delay((100..1000).random().toLong())
        return UsersDb.userSubscriptions[userId] ?: throw UserNotFoundException("User $userId not found.")
    }

    suspend fun getUser(userId: String): UserService.User {
        // simulate database access...
        delay((100..1000).random().toLong())
        return UsersDb.users[userId] ?: throw UserNotFoundException("User $userId not found.")
    }
}
