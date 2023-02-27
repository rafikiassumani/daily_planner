package daily_planner.app.http

import com.linecorp.armeria.common.*
import com.linecorp.armeria.server.annotation.Delete
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.Put
import com.linecorp.armeria.server.annotation.RequestObject
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.Date
import java.util.UUID

class TodoController(
    private val prometheusRegistry : PrometheusMeterRegistry
) {
    private val todos = mutableListOf<Todo>()

    @Post("/todos")
    fun createTodo(@RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.post.todo").increment()
        val todo = Todo(
            id = UUID.randomUUID().toString(),
            title = todoRequest.title,
            description = todoRequest.description,
            createdAt = Date().time,
        )
        todos.add(todo);
        return HttpResponse.ofJson(todo)
    }

    @Get("/todo/{todoId}")
    fun getTodo(@Param("todoId") todoId: String): HttpResponse {
        prometheusRegistry.counter("http.get.todo").increment()

        val todo = todos.firstOrNull { it.id == todoId}

        return if(todo == null) {
            HttpResponse.ofJson(HttpStatus.NOT_FOUND, NotFoundResult("todo not found"))
        } else {
            HttpResponse.ofJson(todo)
        }
    }

    @Put("/todo/{todoId}")
    fun updateTodo(@Param("todoId") todoId: String, @RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.update.todo").increment()
        val todo = todos.firstOrNull { it.id == todoId} ?: return HttpResponse.ofJson(HttpStatus.NOT_FOUND, NotFoundResult("todo not found"))

        todo.apply {
             title = todoRequest.title
             description = todoRequest.description
             modifiedAt = Date().time
             status = todoRequest.status
         }
        return HttpResponse.ofJson(todo)
    }

    @Delete("/todo/{todoId}")
    fun deleteTodo(@Param("todoId") todoId: String ): HttpResponse {
        prometheusRegistry.counter("http.delete.todo").increment()
        val todo = todos.firstOrNull { it.id == todoId} ?: return HttpResponse.ofJson(HttpStatus.NOT_FOUND, NotFoundResult("todo not found"))

        todos.remove(todo)

        return HttpResponse.ofJson( object {
            val success = true
            val message = "successfully deleted ${todo.id}"
        })
    }

    @Get("/todos")
    suspend fun getTodos(): HttpResponse {
        prometheusRegistry.counter("http.get.all.todos").increment()
        val getAllTodosJob = coroutineScope {
           async {
              todos
            }
        }
        return HttpResponse.ofJson(getAllTodosJob.await())

        //return HttpResponse.ofJson(todos)
    }
}

data class Todo(
    var id: String?,
    var title: String,
    var description: String,
    var createdAt: Long? = null,
    var modifiedAt: Long? = null,
    var status: TodoStatus = TodoStatus.CREATED
)

data class NotFoundResult(val message: String)
enum class TodoStatus {
    CREATED, IN_PROGRESS, COMPLETED
}