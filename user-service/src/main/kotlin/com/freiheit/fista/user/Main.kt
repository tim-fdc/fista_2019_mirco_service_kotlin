package com.freiheit.fista.user

import com.freiheit.fista.user.api.ServiceStateGrpcApiImpl
import com.freiheit.fista.user.api.UsersApiGrpc
import com.freiheit.fista.user.api.UsersApiHttp
import io.grpc.BindableService
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.ServerInterceptors
import io.grpc.ServerServiceDefinition
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

const val SERVICE_NAME = "fista-user-service"

private val grpcPort = System.getProperty("USER_SERVICE_GRPC_PORT", "8080").toInt()
private val httpPort = System.getProperty("USER_SERVICE_HTTP_PORT", "8081").toInt()
private val logger = LoggerFactory.getLogger(SERVICE_NAME)

// **********************************************************************************
// Global and instances. Since used from within main only, they are "singleton" used.
// **********************************************************************************

// The API that allows checking the status of the micor service
val serviceStateApi = ServiceStateGrpcApiImpl(LifeCycleService())

// The service processing user requests
val userService = UserService()

// The GRPC API that allows accessing the users
val usersApiGrpc = UsersApiGrpc(userService)

// The HTTP API that allows accessing the users
val usersApiHttp = UsersApiHttp(userService)

/**
 * The main entry point of the service.
 *
 * The service has an HTTP (REST) interface and a GRPC interface (for demonstration purposes). It is not
 * recommended to have both kinds in one service if not really needed (never be dogmatic). The need to migrate
 * from one to the other could be a reason.
 */
fun main() = try {
    // The HTTP server
    startHttpServer()

    // The GRPC server
    startGrpcServer(usersApiGrpc, serviceStateApi)

    //TODO: graceful shutdown!
} catch (e: Exception) {
    logger.error("Failed to start service $SERVICE_NAME", e)
    throw RuntimeException(e)
}

private fun startHttpServer() {
    val server = embeddedServer(Netty, httpPort) { installModule() }
    server.start(wait = false)
    logger.info("HTTP (REST) for $SERVICE_NAME started on port $httpPort.")
}

private fun startGrpcServer(usersApiGrpc: UsersApiGrpc, serviceStateGrpcApi: ServiceStateGrpcApiImpl) {
    val server = ServerBuilder
            .forPort(grpcPort)
            .addService(createGrpcServiceDefinition(usersApiGrpc))
            .addService(createGrpcServiceDefinition(serviceStateGrpcApi))
            // use a fixed thread executor ot make sure memory is bounded.
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
            .build()
    server.start()
    logger.info("GRPC for $SERVICE_NAME started on port $grpcPort")
}

/**
 * Returns a list of interceptors that should intercept each GRPC request. Interceptors are like "HTTP filters".
 * They are usually used for logging, monitoring or checking authentication.
 */
private fun defaultInterceptors(): List<ServerInterceptor> = listOf(LoggingInterceptor(logger))

/**
 * Returns [defaultInterceptors] and a collection of custom [ServerInterceptor].
 */
fun createGrpcServiceDefinition(grpcService: BindableService, vararg interceptors: ServerInterceptor): ServerServiceDefinition {
    val allInterceptors = listOf(*interceptors) + defaultInterceptors()
    return ServerInterceptors.intercept(grpcService, allInterceptors)
}

/**
 * Setup the KTOR server.
 * Ktor is an un-opinionated framework to build asynchronous servers and clients (https://ktor.io/).
 * Modules are installed to an application.
 */
fun Application.installModule() {
    install(ContentNegotiation) {
        // install JSON (GSON) support
        gson {
            setPrettyPrinting()
        }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { e ->
            call.respondText(text = e.message ?: "", status = HttpStatusCode.BadRequest)
        }

        exception<UserService.UserNotFoundException> { e ->
            call.respondText(text = e.message, status = HttpStatusCode.NotFound)
        }

        exception<java.lang.RuntimeException> {
            // do not expose internal information of a RuntimeException
            call.respondText("", status = HttpStatusCode.InternalServerError)
        }
    }

    // install REST resources here
    routing {
        usersApiHttp.createRoutes(this)
    }
}
