package daily_planner.client.http

import com.linecorp.armeria.common.*
import com.linecorp.armeria.server.annotation.*
import daily_planner.client.http.converters.JsonResponseConverter
import daily_planner.client.mappers.TodoMapper
import daily_planner.client.services.TodoServiceClient
import daily_planner.stubs.Todo
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*
import javax.inject.Inject

class TodoController @Inject constructor(
    private val prometheusRegistry : PrometheusMeterRegistry,
    private val todoServiceClient: TodoServiceClient
) {

    @Post("/todos")
    suspend fun createTodo(@RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.post.todo").increment()

        val todoResult = todoServiceClient.createTodo(todoRequest)

        return HttpResponse.ofJson(object {
            val todoId = todoResult.todoId
        })
    }

    @Get("/todo/{todoId}")
    @ResponseConverter(JsonResponseConverter::class)
    suspend fun getTodo(@Param("todoId") todoId: String): Todo {
        prometheusRegistry.counter("http.get.todo").increment()

        val todo = todoServiceClient.getTodo(todoId)

        return TodoMapper().mapTodo(todo)
    }

    @Patch("/todo/{todoId}")
    suspend fun updateTodo(@Param("todoId") id: String, @RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.update.todo").increment()

        val todo = todoRequest.copy(todoId = id)
        val todoIdResult = todoServiceClient.updateTodo(todo)

        return HttpResponse.ofJson(object {
            val todoId = todoIdResult.todoId
        })
    }

    @Delete("/todo/{todoId}")
    suspend fun deleteTodo(@Param("todoId") todoId: String ): HttpResponse {
        prometheusRegistry.counter("http.delete.todo").increment()

        todoServiceClient.deleteTodo(todoId)
        return HttpResponse.ofJson( object {
            val success = true
            val message = "successfully deleted $todoId"
        })

    }

    @Get("/todos/{authorId}")
    @ResponseConverter(JsonResponseConverter::class)
    suspend fun getTodos(@Param("authorId") authorId: String): List<Todo> {
        prometheusRegistry.counter("http.get.all.todos.by.authorId").increment()

        val allTodosByAuthor = todoServiceClient.getAllTodosByAuthor(authorId)
        val mappedTodosByAuthor = allTodosByAuthor.map {
            TodoMapper().mapTodo(it)
        }.toList()

        return mappedTodosByAuthor
    }
}
