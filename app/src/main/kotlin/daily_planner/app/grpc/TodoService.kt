package daily_planner.app.grpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Timestamp
import daily_planner.app.dao.TodoRepository
import daily_planner.app.kafka.TodoKafkaProducer
import daily_planner.stubs.TodoEvent
import daily_planner.stubs.TodoStatus
import daily_planner.stubs.utils.DateUtil.Companion.convertProtoToInstance
import daily_planner.stubs.utils.DateUtil.Companion.convertToProtoTimestamp
import io.grpc.Status
import io.grpc.StatusException
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import todo.app.grpc.TodoOuterClass
import todo.app.grpc.TodoOuterClass.ID
import todo.app.grpc.TodoServiceGrpcKt
import java.util.*
import javax.inject.Inject

class TodoService @Inject constructor (
    private val registry: PrometheusMeterRegistry,
    private val jsonMapper: ObjectMapper,
    private val repository: TodoRepository
) : TodoServiceGrpcKt.TodoServiceCoroutineImplBase() {
    companion object {
        const val brokers = "broker-service:9092"
        const val topic = "todos"
    }
    override suspend fun createTodo(request: TodoOuterClass.Todo): ID {
        registry.counter("grpc.create.todo").increment()

        // replace with db Save
         val todo = repository.createTodo(
             title = request.title,
             description = request.description,
             authorId = request.authorId,
             plannedAt = convertProtoToInstance(request.plannedAt),
             status = TodoStatus.valueOf(request.status.name)
         )
        //record created todo kafka event
        TodoKafkaProducer(brokers, jsonMapper).produce(topic,
            TodoEvent(
                eventType = "CREATE_TODO",
                todoData = todo
            )
        )
        return ID
            .newBuilder()
            .setTodoId(todo.todoId)
            .build()

    }

    override suspend fun getTodo(request: ID): TodoOuterClass.Todo {
        registry.counter("grpc.ger.todo").increment()
        val todo = repository.findTodoById(UUID.fromString(request.todoId))
            ?: throw StatusException(Status.NOT_FOUND.withDescription("todo with ${request.todoId} id not found"))


        val response =  TodoOuterClass.Todo.newBuilder()
            .setTodoId(todo.todoId)
            .setAuthorId(todo.authorId)
            .setTitle(todo.title)
            .setDescription(todo.description)
            .setCreatedAt(convertToProtoTimestamp(todo.createdAt))
            .setStatus(TodoOuterClass.Todo.TODO_STATUS.forNumber(todo.status.ordinal))

         if (todo.updatedAt != null) {
             response.updatedAt = Timestamp.newBuilder()
                 .setSeconds(todo.updatedAt?.epochSecond!!)
                 .setNanos(todo.updatedAt?.nano!!)
                 .build()
         }

        if (todo.plannedAt != null) {
            response.plannedAt = convertToProtoTimestamp(todo.plannedAt)
        }

        if (todo.completedAt != null) {
            response.completedAt = convertToProtoTimestamp(todo.completedAt)
        }

        return response.build()
    }

    override suspend fun updateTodo(request: TodoOuterClass.Todo): ID {
        registry.counter("grpc.update.todo").increment()
        val todo = repository.findTodoById(UUID.fromString(request.todoId))
            ?: throw StatusException(Status.NOT_FOUND.withDescription("todo with ${request.todoId} id not found"))

        // record updated todo kafka event  {tod_id, action}
        // send email using workflow to notify owner that to do was updated. Might also need to
        // send email containing stats of all todos created and updated.
        val updatedTodo = repository.updateTodo(
            todoId = UUID.fromString(todo.todoId),
            title = request.title,
            description = request.description,
            authorId = request.authorId,
            plannedAt = convertProtoToInstance(request.plannedAt),
            completedAt = convertProtoToInstance(request.completedAt),
            status =  TodoStatus.valueOf(request.status.name)
        )
       // send update kafka event
        TodoKafkaProducer(brokers, jsonMapper).produce(
            topic,
            TodoEvent(
                eventType = "UPDATE_TODO",
                todoData = updatedTodo
            )
        )

        return ID.newBuilder().setTodoId(updatedTodo.todoId).build()
    }

    override suspend fun deleteTodo(request: ID): ID {
        registry.counter("grpc.delete.todo").increment()
        val todo = repository.findTodoById(UUID.fromString(request.todoId))
            ?: throw StatusException(Status.NOT_FOUND.withDescription("todo with ${request.todoId} id not found"))

        //record deleted todo kafka event {tod_id, action}
        repository.deleteById(UUID.fromString(todo.todoId))
        TodoKafkaProducer(brokers, jsonMapper).produce(
            topic,
            TodoEvent(
                eventType = "DELETE_TODO",
                todoData = todo
            )
        )

        return ID.newBuilder().setTodoId(todo.todoId).build()

    }

    override fun getAllTodosByAuthor(request: TodoOuterClass.AuthorIdRequest): Flow<TodoOuterClass.Todo> {
        registry.counter("grpc.stream.all.todos").increment()

        val todos = repository.listTodosByAuthor(request.authorId)

         return todos.map {
             val responseBuilder =  TodoOuterClass.Todo.newBuilder()
                 .setTodoId(it.todoId)
                 .setAuthorId(it.authorId)
                 .setTitle(it.title)
                 .setDescription(it.description)
                 .setStatus(TodoOuterClass.Todo.TODO_STATUS.forNumber(it.status.ordinal))

             if (it.updatedAt != null) {
                 responseBuilder.updatedAt = convertToProtoTimestamp(it.updatedAt)
             }

             if (it.createdAt != null) {
                 responseBuilder.createdAt = convertToProtoTimestamp(it.createdAt)
             }

             if (it.plannedAt != null) {
                 responseBuilder.plannedAt = convertToProtoTimestamp(it.plannedAt)
             }

             if (it.completedAt != null) {
                 responseBuilder.completedAt = convertToProtoTimestamp(it.plannedAt)
             }
             responseBuilder.build()
        }.asFlow()

    }
}