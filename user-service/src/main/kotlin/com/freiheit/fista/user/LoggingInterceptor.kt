package com.freiheit.fista.user

import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.slf4j.Logger

/**
 * Intercepts incoming requests and logs the call duration.
 */
class LoggingInterceptor(val logger: Logger) : ServerInterceptor {

    override
    fun <ReqT, RespT> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
        val start = System.currentTimeMillis()
        val name = call.methodDescriptor.fullMethodName
        logger.debug("$name - request received")

        // wrap call into a forwarder to add status logging after close
        val forwardCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun close(status: Status?, trailers: Metadata?) {
                super.close(status, trailers)
                val statusCode = status?.code ?: "unknown"
                val statusDescription = if (status?.description != null) " - ${status.description}" else ""
                val statusMessage = "$statusCode$statusDescription"
                if (status != null && !status.isOk) {
                    logger.info("$name - request failed with code $statusMessage")
                } else {
                    val duration = System.currentTimeMillis() - start
                    logger.info("$name - request completed with code $statusMessage in $duration [ms]")
                }
            }
        }

        val listener = next.startCall(forwardCall, headers)
        // close will not be called for cancelled requests, handle logging for these cases with a listener
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onCancel() {
                logger.info("$name - request was cancelled")
                super.onCancel()
            }
            // onHalfClose and onComplete not implemented as they are covered above already
            // (by [ForwardingServerCall.close] with more information like the status)
        }
    }
}