package com.freiheit.fista.user

import io.grpc.Status
import io.grpc.StatusRuntimeException

/**
 * Little abstraction to easier
 */
interface GrpcMappableException {
    fun toGrpcException(): StatusRuntimeException
}

/**
 * Throw this exception if a parameter was invalid.
 */
open class BadParameterException(msg: String) : GrpcMappableException, Exception(msg) {
    override fun toGrpcException(): StatusRuntimeException {
        return Status.INVALID_ARGUMENT.withDescription(this.message).asRuntimeException()
    }
}
