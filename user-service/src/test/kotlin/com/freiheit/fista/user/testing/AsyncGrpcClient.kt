package com.freiheit.fista.user.testing

import com.freiheit.fista.user.async.suspendingUnaryCallObserver
import com.freiheit.fista.user.grpc.GetUsersReq
import com.freiheit.fista.user.grpc.GetUsersRsp
import com.freiheit.fista.user.grpc.UserApiGrpc

/**
 * For each API function create this little extension as a helper to also asynchronously call the server's api.
 *
 * Extends the [UserApiGrpc.UserApiStub] by a function, but only takes the request as parameter. The GRPC observer
 * is then created using function [suspendingUnaryCallObserver].
 */
suspend fun UserApiGrpc.UserApiStub.getUsers(request: GetUsersReq): GetUsersRsp =
        suspendingUnaryCallObserver { observer -> getUsers(request, observer) }