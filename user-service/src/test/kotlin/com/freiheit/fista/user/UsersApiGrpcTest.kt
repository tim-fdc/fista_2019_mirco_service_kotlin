package com.freiheit.fista.user

import com.freiheit.fista.user.GlobalTestData.testUserBetty
import com.freiheit.fista.user.GlobalTestData.testUserBettyId
import com.freiheit.fista.user.api.UsersApiGrpc
import com.freiheit.fista.user.grpc.GetUsersReq
import com.freiheit.fista.user.grpc.UserApiGrpc
import com.freiheit.fista.user.testing.InProcessServer
import com.freiheit.fista.user.testing.getUsers
import io.grpc.inprocess.InProcessChannelBuilder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * If your test data is global name them accordingly. Global state will eventually change and each developer (also
 * you and you in some weeks) should know that the data is globally used.
 */
object GlobalTestData {
    const val testUserBettyId = "test id 1"
    val testUserBetty = UserService.User(id = testUserBettyId, email = "betty")
}

/**
 * Spek is a library that allows writing tests in a "fluid" (readable) way (https://spekframework.org/).
 *
 * This test, tests the API layer. Any layer below the API layer is mocked.
 */
object UsersApiGrpcTest : Spek({
    given("a GRPC api to read users") {
        // Use mockk (https://github.com/mockk/mockk) to mock dependencies. Mockk is a powerful and
        // still clear and understandable mocking framework. Use it! (And star it on GitHub).
        val service = mockk<UserService>()

        // Test the API with the mocked service. Follow the matryoshka principle.
        val api = UsersApiGrpc(service)

        // To provide the API a server is needed.
        val grpcServer = InProcessServer(createGrpcServiceDefinition(api))

        // Create a channel to your in process server
        val channel = InProcessChannelBuilder
                .forName(grpcServer.name)
                .directExecutor()
                .build()

        // This is the API to test!
        val apiStub = UserApiGrpc.newStub(channel)

        beforeGroup {
            grpcServer.start()
        }

        afterGroup {
            grpcServer.stop()
        }

        on("reading the users") {
            it("should return an empty list if no users are found") {
                coEvery { service.getUsers() } returns listOf()
                val users = runBlocking {
                    return@runBlocking apiStub.getUsers(GetUsersReq.newBuilder().build())
                }
                users shouldNotBe null
                users.usersCount shouldEqualTo 0
            }

            it("should return users if there are any") {
                coEvery { service.getUsers() } returns listOf(testUserBetty)

                val users = runBlocking {
                    return@runBlocking apiStub.getUsers(GetUsersReq.newBuilder().build())
                }
                users shouldNotBe null
                users.usersCount shouldEqualTo 1
                users.usersList.get(0).id shouldEqualTo testUserBettyId
            }

            // TODO: your tests!
        }
    }
})
