package daily_planner.app.http

import com.linecorp.armeria.common.*
import com.linecorp.armeria.server.annotation.*
import daily_planner.app.dao.TodoRepository
import daily_planner.app.kafka.TodoKafkaProducer
import daily_planner.stubs.Todo
import daily_planner.stubs.TodoEvent
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.util.*
import javax.inject.Inject

class TodoController @Inject constructor(
    private val prometheusRegistry : PrometheusMeterRegistry,
    private val kafkaProducer: TodoKafkaProducer,
    private val todoRepository: TodoRepository
) {
    private val todos = mutableListOf<Todo>()

    @Post("/todos")
    suspend fun createTodo(@RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.post.todo").increment()

        val todo  = todoRepository.createTodo(
                title = todoRequest.title,
                description = todoRequest.description,
                authorId = todoRequest.authorId,
                plannedAt = todoRequest.plannedAt?.let {Date.from(Instant.ofEpochSecond(it.time))},
                completedAt = todoRequest.completedAt?.let {Date.from(Instant.ofEpochSecond(it.time))},
                updatedAt = todoRequest.updatedAt?.let {Date.from(Instant.ofEpochSecond(it.time))},
                status = todoRequest.status
            )
        //Produce kafka event
        kafkaProducer.produce("todos",
                TodoEvent(
                    eventType = "CREATE_TODO",
                    todoData = todo
                )
          )

        return HttpResponse.ofJson(todo)
    }

    @Get("/todo/{todoId}")
    fun getTodo(@Param("todoId") todoId: String): HttpResponse {
        prometheusRegistry.counter("http.get.todo").increment()

        val todo = todoRepository.findTodoById(UUID.fromString(todoId))

        todo?.let {
            return HttpResponse.ofJson(it)
        }

        return HttpResponse.ofJson( object {
            val message = "Not found"
        })
    }

    @Put("/todo/{todoId}")
    suspend fun updateTodo(@Param("todoId") todoId: String, @RequestObject todoRequest: Todo): HttpResponse {
        prometheusRegistry.counter("http.update.todo").increment()
        val todo = todoRepository.findTodoById(UUID.fromString(todoId))

        if (todo != null) {
            val updatedTodo = todoRepository.updateTodo(
                todoId = UUID.fromString(todoId),
                title = todoRequest.title,
                description = todoRequest.description,
                authorId = todoRequest.authorId,
                plannedAt = todoRequest.plannedAt?.let {Date.from(Instant.ofEpochSecond(it.time))},
                completedAt = todoRequest.completedAt?.let {Date.from(Instant.ofEpochSecond(it.time))},
                status = todoRequest.status
            )
            //Produce kafka event
            kafkaProducer.produce("todos",
                TodoEvent(
                    eventType = "UPDATE_TODO",
                    todoData = todo
                )
            )
            return HttpResponse.ofJson(updatedTodo)
        }

        return HttpResponse.ofJson( object {
            val success = false
            val message = "Unable to update todo with the following id $todoId"
        })

    }

    @Delete("/todo/{todoId}")
    fun deleteTodo(@Param("todoId") todoId: String ): HttpResponse {
        prometheusRegistry.counter("http.delete.todo").increment()
        val todo = todoRepository.findTodoById(UUID.fromString(todoId))

        if (todo != null) {
            todoRepository.deleteById(UUID.fromString(todo.todoId))
            return HttpResponse.ofJson( object {
                val success = true
                val message = "successfully deleted ${todo.todoId}"
            })
        }
        return HttpResponse.ofJson( object {
            val success = false
            val message = "Unable to delete the todo with the following $todoId"
        })
    }

    @Get("/todos/{authorId}")
    suspend fun getTodos(@Param("authorId") authorId: String): HttpResponse {
        prometheusRegistry.counter("http.get.all.todos.by.authorId").increment()
        val getAllTodosByAuthor = coroutineScope {
           async {
               todoRepository.listTodosByAuthor(authorId)
            }
        }
        return HttpResponse.ofJson(getAllTodosByAuthor.await())
    }
}

data class NotFoundResult(val message: String)
