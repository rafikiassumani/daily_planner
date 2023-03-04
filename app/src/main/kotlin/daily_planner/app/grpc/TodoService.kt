package daily_planner.app.grpc

import daily_planner.app.kafka.TodoKafkaProducer
import daily_planner.stubs.Todo
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.Clock
import todo.app.grpc.TodoOuterClass
import todo.app.grpc.TodoOuterClass.ID
import todo.app.grpc.TodoServiceGrpcKt
import java.util.UUID

class TodoService (private val registry: PrometheusMeterRegistry)
    : TodoServiceGrpcKt.TodoServiceCoroutineImplBase() {
    companion object {
        val todoList = mutableListOf<TodoOuterClass.Todo>()
        const val brokers = "localhost:9092"
        const val topic = "todos"
    }
    override suspend fun createTodo(request: TodoOuterClass.Todo): ID {
        registry.counter("grpc.create.todo").increment()

        val todo = TodoOuterClass.Todo.newBuilder()
            .setTodoId(UUID.randomUUID().toString())
            .setAuthorId(request.authorId)
            .setTitle(request.title)
            .setDescription(request.description)
            .setCreatedAt(Clock.System.now().toEpochMilliseconds())
            .setStatus(TodoOuterClass.Todo.TODO_STATUS.CREATED)
            .build()
        // replace with db Save
        todoList.add(todo)
        //record created todo kafka event
        TodoKafkaProducer(brokers).produce(topic,
            Todo(
                id = todo.todoId,
                title = todo.title,
                description = todo.description,
                createdAt = todo.createdAt,
                status = todo.status.toString()
         )
        )
        return ID
            .newBuilder()
            .setTodoId(todo.todoId)
            .build()

    }

    override suspend fun getTodo(request: ID): TodoOuterClass.Todo {
        registry.counter("grpc.ger.todo").increment()
        val todo = todoList.firstOrNull { it.todoId == request.todoId }

        return if ( todo != null) {
            TodoOuterClass.Todo.newBuilder()
                .setTodoId(UUID.randomUUID().toString())
                .setAuthorId(todo.authorId)
                .setTitle(todo.title)
                .setDescription(todo.description)
                .setCreatedAt(todo.createdAt)
                .setStatus(TodoOuterClass.Todo.TODO_STATUS.CREATED)
                .build()
        } else {
            TodoOuterClass.Todo.newBuilder()
                .build()
        }
    }

    override suspend fun updateTodo(request: TodoOuterClass.Todo): ID {
        registry.counter("grpc.update.todo").increment()
        val todo = todoList.firstOrNull { it.todoId == request.todoId }

        return if(todo != null) {
            val updatedTodo = TodoOuterClass.Todo.newBuilder()
                .setAuthorId(todo.authorId)
                .setTitle(todo.title)
                .setDescription(todo.description)
                .setCreatedAt(todo.createdAt)
                .setStatus(request.status)
                .build()

            // record updated todo kafka event  {tod_id, action}
            // send email using workflow to notify owner that to do was updated. Might also need to
            // send email containing stats of all todos created and updated.
            todoList.add(updatedTodo)
            //send update kafka event
            TodoKafkaProducer(brokers).produce(
                topic,
                Todo(
                    id = todo.todoId,
                    title = todo.title,
                    description = todo.description,
                    createdAt = todo.createdAt,
                    status = todo.status.toString()
                ))
            ID.newBuilder().setTodoId(todo.todoId).build()
        } else {
            ID.newBuilder().setTodoId("-1").build()
        }
    }

    override suspend fun deleteTodo(request: ID): ID {
        registry.counter("grpc.delete.todo").increment()
        val todo = todoList.firstOrNull { it.todoId == request.todoId }

        return if(todo != null) {

            //record deleted todo kafka event {tod_id, action}
            todoList.remove(todo)
            ID.newBuilder().setTodoId(todo.todoId).build()
        } else {
            ID.newBuilder().setTodoId("-1").build()
        }
    }

    override fun getAllTodos(request: TodoOuterClass.GetTodosRequest): Flow<TodoOuterClass.Todo> {
        //async stream. Client needs to use collect method. what about non kotlin clients ?
        registry.counter("grpc.stream.all.todos").increment()
        return todoList.asFlow()
    }
}