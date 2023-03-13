package daily_planner.app.grpc

import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.Flow
import todo.app.grpc.UserOuterClass
import todo.app.grpc.UserServiceGrpcKt.UserServiceCoroutineImplBase
import javax.inject.Inject

class UserService @Inject constructor(
    private val registry: PrometheusMeterRegistry
 ) :
    UserServiceCoroutineImplBase() {
    override suspend fun createUser(request: UserOuterClass.User): UserOuterClass.USER_ID {
        registry.counter("grpc.create.user").increment()
        return super.createUser(request)
    }

    override suspend fun getUser(request: UserOuterClass.USER_ID): UserOuterClass.User {
        registry.counter("grpc.get.user").increment()
        return super.getUser(request)
    }

    override suspend fun updateUser(request: UserOuterClass.User): UserOuterClass.USER_ID {
        registry.counter("grpc.update.user").increment()
        return super.updateUser(request)
    }

    override suspend fun deleteUser(request: UserOuterClass.USER_ID): UserOuterClass.USER_ID {
        registry.counter("grpc.delete.user").increment()
        return super.deleteUser(request)
    }

    override fun getAllUsers(request: UserOuterClass.GetUsersRequest): Flow<UserOuterClass.User> {
        return super.getAllUsers(request)
    }
}