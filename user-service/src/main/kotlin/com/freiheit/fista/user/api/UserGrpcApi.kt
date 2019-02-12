package com.freiheit.fista.user.api

import com.freiheit.fista.user.UserService
import com.freiheit.fista.user.async.processGrpcRequest
import com.freiheit.fista.user.async.requestContext
import com.freiheit.fista.user.grpc.GetUsersReq
import com.freiheit.fista.user.grpc.GetUsersRsp
import com.freiheit.fista.user.grpc.User
import com.freiheit.fista.user.grpc.UserApiGrpc
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

/**
 * Implementation of the users GRPC API as defined in users.proto.
 */
class UsersApiGrpc(private val service: UserService) : UserApiGrpc.UserApiImplBase() {
    private val logger = LoggerFactory.getLogger(UsersApiGrpc::class.java)

    /**
     * This function is the implementation of the getUsers API call. As specified in the users.proto it takes
     * a request param and returns a result. Since GRPC works with channels to facilitate asynchronous request
     * processing, each function gets a responseObserver to "call back" once finished (or failed). This "magic"
     * happens in [processGrpcRequest].
     */
    override fun getUsers(request: GetUsersReq, responseObserver: StreamObserver<GetUsersRsp>) {
        processGrpcRequest(responseObserver) {
            getUsers()
        }
    }

    /**
     * This function exists to demonstrate accessing the [coroutineScope] (it could be easily part of [getUser]).
     * The function switches into the scope and takes the callTreeId to log it.
     *
     * Notice: param request is not passed into this function. Pass if needed only.
     */
    private suspend fun getUsers() = coroutineScope {
        logger.info("${requestContext().callTreeId} received get users")
        return@coroutineScope service.getUsers().toApiModel()
    }

    private fun Collection<UserService.User>.toApiModel(): GetUsersRsp = GetUsersRsp.newBuilder()
            .addAllUsers(
                    map {
                        User.newBuilder()
                                .setId(it.id)
                                .setNickname(it.email)
                                .build()
                    }
            ).build()
}
