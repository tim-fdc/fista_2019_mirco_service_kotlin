package com.freiheit.fista.user.api

import com.freiheit.fista.user.Healthiness
import com.freiheit.fista.user.Readiness
import com.freiheit.fista.user.LifeCycleService
import com.freiheit.fista.user.async.processGrpcRequest
import com.freiheit.fista.user.grpc.HealthReq
import com.freiheit.fista.user.grpc.HealthRsp
import com.freiheit.fista.user.grpc.HealthState
import com.freiheit.fista.user.grpc.ReadyReq
import com.freiheit.fista.user.grpc.ReadyRsp
import com.freiheit.fista.user.grpc.ReadyState
import com.freiheit.fista.user.grpc.ServiceStateApiGrpc
import io.grpc.stub.StreamObserver

/**
 * Each micro service should make its healthiness and readiness accessible to the outside. Cluster management systems such
 * as Kubernetes use the health endpoint to permanently check if a service is still healthy or if the service should be
 * shut down and a new instance should be created instead. Readiness is needed during deployments.
 *
 * At any given time a certain number of micro services should be available to handle requests. A deployment must
 * therefore shut down n instances only if n new instances are up and ready.
 *
 * Of course is n bounded. If m is the total number of replicas of a single micro service n < m-2.
 *
 * The service implements [ServiceStateApiGrpc.ServiceStateApiImplBase], which is generated by the GRPC plugin according
 * to the definition of the users.proto.
 */
class ServiceStateGrpcApiImpl(private val stateService: LifeCycleService) : ServiceStateApiGrpc.ServiceStateApiImplBase() {
    /**
     * Returns the health state of the service.
     *
     * Ideally, [health] and [ready] are called via a separate channel (connector). Assuming there are too many
     * requests already piling up. Any request to check [health] and [ready] would also just pile up.
     */
    override fun health(request: HealthReq, responseObserver: StreamObserver<HealthRsp>) {
        processGrpcRequest(responseObserver) {
            stateService.healthy().toApiModel()
        }
    }

    /**
     * Returns the ready state of the service.
     */
    override fun ready(request: ReadyReq, responseObserver: StreamObserver<ReadyRsp>) {
        processGrpcRequest(responseObserver) {
            stateService.ready().toApiModel()
        }
    }

    /**
     * Transforms between the API <> SERVICE layer.
     */
    private fun Healthiness.toApiModel(): HealthRsp {
        val builder = HealthRsp.newBuilder()
        when (this) {
            Healthiness.HEALTHY -> builder.state = HealthState.HEALTHY
            Healthiness.FAILURE -> builder.state = HealthState.UN_HEALTHY
        }
        return builder.build()
    }

    /**
     * Transforms between the API <> SERVICE layer.
     */
    private fun Readiness.toApiModel(): ReadyRsp {
        val builder = ReadyRsp.newBuilder()
        when (this) {
            Readiness.STARTING -> builder.state = ReadyState.STARTING
            Readiness.READY -> builder.state = ReadyState.READY
            Readiness.FAILURE -> builder.state = ReadyState.FAILURE
        }
        return builder.build()
    }
}