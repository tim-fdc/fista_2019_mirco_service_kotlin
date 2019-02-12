package com.freiheit.fista.todo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object TodoServiceTest : Spek({
    given("a todo service") {
        val mockPort = 8083
        val mockHttpServer = WireMockServer(
                WireMockConfiguration.wireMockConfig()
                        .port(mockPort)
                        .containerThreads(10)
                        .asynchronousResponseEnabled(true))
        val testUserId = "73b020ae-6b8a-4b6f-875c-0a41bc3d928e"
        val baseUrl = "http://localhost:$mockPort"
        val mockUserJson = """
        [
            {
                id: "$testUserId",
                email: "herbie"
            },
            {
                id: "d3760a9c-dd95-4f5a-abf3-d826fc6a3649",
                email: "jacky"
            },
            {
                id: "1d8c1cad-bc97-48d1-9c43-05d69dfb3f1a",
                email: "betty"
            }
        ]""".trimIndent()

        val todoService = TodoService(
                store = mockk(),
                httpClient = httpClientFactory(),
                config = TodoService.Config(userServiceUrl = baseUrl)
        )

        beforeGroup {
            mockHttpServer.start()
        }

        afterEachTest {
            mockHttpServer.resetAll()
        }

        afterGroup {
            mockHttpServer.stop()
        }

        on("on creating a todo") {
            it("should read a list of users") {
                val usersUrl = "/users/"
                mockHttpServer.stubFor(
                        WireMock.get(WireMock.urlPathMatching(usersUrl))
                                .willReturn(WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(mockUserJson)))

                runBlocking {
                    val create = todoService.create(testUserId, "some todo")
                    create shouldNotBe null
                }

                mockHttpServer.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(usersUrl)))
            }

            it("should return NOT FOUND and not create a todo if external service was not found") {
                val usersUrl = "/users/"
                mockHttpServer.stubFor(
                        WireMock.get(WireMock.urlPathMatching(usersUrl))
                                .willReturn(WireMock.aResponse()
                                        .withStatus(404)))

                runBlocking {
                    try {
                        todoService.create(testUserId, "some todo")
                    } catch (e: Exception) {
                        e shouldBeInstanceOf TodoService.HttpClientException::class
                        e.message shouldEqual "HTTP Error 404 Not Found"
                    }
                }

                mockHttpServer.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(usersUrl)))
            }

            it("should not create a todo if the user was not found") {
                val usersUrl = "/users/"
                mockHttpServer.stubFor(
                        WireMock.get(WireMock.urlPathMatching(usersUrl))
                                .willReturn(WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(mockUserJson)))

                runBlocking {
                    try {
                        todoService.create("some user id", "some todo")
                    } catch (e: TodoService.UserNotFoundException) {
                        e shouldBeInstanceOf TodoService.UserNotFoundException::class
                        e.message shouldEqual "User some user id does not exist."
                    }
                }

                mockHttpServer.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(usersUrl)))
            }
        }
    }
})