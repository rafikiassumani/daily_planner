package daily_planner.app.grpc

import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.Clock
import todo.app.grpc.TodoOuterClass
import todo.app.grpc.TodoOuterClass.Todo
import todo.app.grpc.TodoOuterClass.ID
import todo.app.grpc.TodoServiceGrpcKt
import java.util.Date
import java.util.UUID

class TodoService (private val registry: PrometheusMeterRegistry)
    : TodoServiceGrpcKt.TodoServiceCoroutineImplBase() {
    companion object {
        val todoList = mutableListOf<Todo>()
    }
    override suspend fun createTodo(request: Todo): ID {
        registry.counter("grpc.create.todo").increment()

        val todo = Todo.newBuilder()
            .setTodoId(UUID.randomUUID().toString())
            .setAuthorId(request.authorId)
            .setTitle(request.title)
            .setDescription(request.description)
            .setCreatedAt(Clock.System.now().toEpochMilliseconds())
            .setStatus(Todo.TODO_STATUS.CREATED)
            .build()

       //record created todo kafka event
       //record prometheus todo count
        todoList.add(todo)
        return ID
            .newBuilder()
            .setTodoId(todo.todoId)
            .build()

    }

    override suspend fun getTodo(request: ID): Todo {
        registry.counter("grpc.ger.todo").increment()
        val todo = todoList.firstOrNull { it.todoId == request.todoId }

        return if ( todo != null) {
            Todo.newBuilder()
                .setTodoId(UUID.randomUUID().toString())
                .setAuthorId(todo.authorId)
                .setTitle(todo.title)
                .setDescription(todo.description)
                .setCreatedAt(todo.createdAt)
                .setStatus(Todo.TODO_STATUS.CREATED)
                .build()
        } else {
            Todo.newBuilder()
                .build()
        }
    }

    override suspend fun updateTodo(request: Todo): ID {
        registry.counter("grpc.update.todo").increment()
        val todo = todoList.firstOrNull { it.todoId == request.todoId }

        return if(todo != null) {
            val updatedTodo = Todo.newBuilder()
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

    override fun getAllTodos(request: TodoOuterClass.GetTodosRequest): Flow<Todo> {
        //async stream. Client needs to use collect method. what about non kotlin clients ?
        registry.counter("grpc.stream.all.todos").increment()
        return todoList.asFlow()
    }
}