package com.freiheit.fista.user.testing

import io.grpc.Server
import io.grpc.ServerServiceDefinition
import io.grpc.inprocess.InProcessServerBuilder

/**
 * Server that runs within the current process. Use only in your tests.
 */
class InProcessServer(service: ServerServiceDefinition, val name: String = "testing") {

    private val server: Server = InProcessServerBuilder
            .forName(name)
            .directExecutor()
            .addService(service)
            .build()

    fun start() {
        server.start()
        Runtime.getRuntime().addShutdownHook(Thread {
            println("JVM shutdown... stopping gRPC server")
            stop()
            println("*** server shut down")
        })
    }

    fun stop() {
        server.shutdown()
    }
}