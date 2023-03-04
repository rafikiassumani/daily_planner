package daily_planner.app.http

import com.linecorp.armeria.common.*
import com.linecorp.armeria.server.annotation.Delete
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.Put
import com.linecorp.armeria.server.annotation.RequestObject
import daily_planner.app.kafka.TodoKafkaProducer
import daily_planner.stubs.Todo
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class TodoController(
    private val prometheusRegistry : PrometheusMeterRegistry
) {
    private val todos = mutableListOf<Todo>()

    @Post("/todos")
    suspend fun createTodo(@RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.post.todo").increment()
        val todo = Todo(
            id = UUID.randomUUID().toString(),
            title = todoRequest.title,
            description = todoRequest.description,
            createdAt = Date().time,
        )
        //Produce kafka event
        withContext(Dispatchers.IO) {
            TodoKafkaProducer("localhost:9092").produce("todos",
                Todo(
                    id = todo.id,
                    title = todo.title,
                    description = todo.description,
                    createdAt = todo.createdAt,
                    status = todo.status
                ))
        }
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
    suspend fun updateTodo(@Param("todoId") todoId: String, @RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.update.todo").increment()
        val todo = todos.firstOrNull { it.id == todoId} ?: return HttpResponse.ofJson(HttpStatus.NOT_FOUND, NotFoundResult("todo not found"))

        todo.apply {
             title = todoRequest.title
             description = todoRequest.description
             modifiedAt = Date().time
             status = todoRequest.status
         }
        //Produce kafka event
        //Do we need armeria coroutine context ????
        withContext(Dispatchers.IO) {
            TodoKafkaProducer("localhost:9092").produce("todos",
                Todo(
                    id = todo.id,
                    title = todo.title,
                    description = todo.description,
                    createdAt = todo.createdAt,
                    status = todo.status
                ))
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

data class NotFoundResult(val message: String)
