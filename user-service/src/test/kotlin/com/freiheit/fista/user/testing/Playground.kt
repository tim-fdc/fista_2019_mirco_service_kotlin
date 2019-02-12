package com.freiheit.fista.user.testing

import com.freiheit.fista.user.grpc.GetUsersReq
import com.freiheit.fista.user.grpc.GetUsersRsp
import com.freiheit.fista.user.grpc.UserApiGrpc
import io.grpc.ManagedChannelBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.util.cio.toByteArray
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executors

/**
 * The Playground contains some code to manually and easily play with the APIs...
 */

val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

fun main() {
    val client = httpClientFactory()
    try {
        runBlocking {
            println(getUsersViaGrpc())
        }

        runBlocking {
            println(client.getUsersViaHttp())
        }
    } finally {
        client.close()
        executor.close()
    }
}

fun httpClientFactory() = HttpClient(engineFactory = Apache) {
    engine {
        followRedirects = true
        socketTimeout = 5000
        connectTimeout = 5000
        connectionRequestTimeout = 3
        threadsCount = 1
    }
}

suspend fun getUsersViaGrpc(): GetUsersRsp {
    val stub = UserApiGrpc.newStub(
            ManagedChannelBuilder
                    .forAddress(
                            "localhost",
                            8080)
                    //do not use security here for testing. You need proper certificates etc. to make
                    //this secure.
                    .usePlaintext()
                    .build())

    return stub.getUsers(GetUsersReq.newBuilder().build())
}

data class HttpClientException(val response: HttpResponse) : IOException("HTTP Error ${response.status}")

suspend fun HttpClient.getUsersViaHttp(): String {
    return withContext(executor) {
        try {

            val call = call {
                url("http://localhost:8081/users")
                method = HttpMethod.Get
            }

            if (!call.response.status.isSuccess()) {
                throw HttpClientException(call.response)
            }
            return@withContext String(call.response.content.toByteArray())
        } catch (e: Exception) {
            throw e
        }
    }
}
