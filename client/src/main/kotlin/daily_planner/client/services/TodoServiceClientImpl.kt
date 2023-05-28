package daily_planner.client.services

import com.google.protobuf.Timestamp
import kotlinx.coroutines.flow.Flow
import todo.app.grpc.TodoOuterClass.AuthorIdRequest
import todo.app.grpc.TodoOuterClass.ID
import todo.app.grpc.TodoOuterClass.Todo
import todo.app.grpc.TodoServiceGrpcKt.TodoServiceCoroutineStub
import javax.inject.Inject

class TodoServiceClientImpl @Inject constructor (
    private val todoGrpcClient: TodoServiceCoroutineStub
) : TodoServiceClient {
    override suspend fun createTodo(todo: daily_planner.stubs.Todo): ID {

        val req = Todo.newBuilder().setTitle(todo.title)
            .setDescription(todo.description)
            .setAuthorId(todo.authorId)
            .setStatus(Todo.TODO_STATUS.forNumber(todo.status.ordinal))

       if( todo.plannedAt != null) {
           req.plannedAt = Timestamp.newBuilder()
               .setSeconds(todo.plannedAt?.epochSecond!!)
               .setNanos(todo.plannedAt?.nano!!).build()
       }

       return todoGrpcClient.createTodo(req.build())
    }

    override suspend fun getTodo(todoUUID: String): Todo {
        val todoRequestID = ID.newBuilder().apply {
            todoId = todoUUID
        }.build()

        return todoGrpcClient.getTodo(todoRequestID)
    }

    override suspend fun updateTodo(todo: daily_planner.stubs.Todo): ID {
        val todoRequest = Todo.newBuilder().apply {
            todoId = todo.todoId
            title = todo.title
            description = todo.description
            authorId = todo.authorId
            status = Todo.TODO_STATUS.forNumber(todo.status.ordinal)
        }

        if (todo.plannedAt != null) {
            todoRequest.plannedAt = Timestamp.newBuilder()
                .setSeconds(todo.plannedAt?.epochSecond!!)
                .setNanos(todo.plannedAt?.nano!!)
                .build()
        }

        return todoGrpcClient.updateTodo(todoRequest.build())
    }

    override suspend fun deleteTodo(todoUUID: String): ID {
        val todoRequestID = ID.newBuilder().apply {
            todoId = todoUUID
        }.build()

        return todoGrpcClient.deleteTodo(todoRequestID)
    }

    override fun getAllTodosByAuthor(authorId: String): Flow<Todo> {
       val todoAuthorRequest = AuthorIdRequest.newBuilder().setAuthorId(authorId).build()

       return todoGrpcClient.getAllTodosByAuthor(todoAuthorRequest)
    }
}