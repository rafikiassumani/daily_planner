package daily_planner.app.dao

import daily_planner.stubs.Todo
import daily_planner.stubs.TodoStatus
import org.jdbi.v3.core.Jdbi
import java.time.Instant
import java.util.*
import javax.inject.Named

class TodoRepository(@Named("todoJdbi") private val jdbi : Jdbi ): TodoDao {

    override fun createTable() {
        jdbi.withExtension<Unit, TodoDao, Exception>(TodoDao::class.java) {
            it.createTable()
        }
    }

    override fun createTodo(
        title: String,
        description: String,
        authorId: String?,
        status: TodoStatus,
        completedAt: Instant?,
        plannedAt: Instant?,
        updatedAt: Instant?,
    ) : Todo {
        return jdbi.withExtension<Todo, TodoDao, Exception>(TodoDao::class.java) {
            it.createTodo(
                title,
                description,
                authorId,
                status,
                completedAt,
                plannedAt,
                updatedAt
            )
        }
    }

    override fun updateTodo(
        todoId: UUID,
        authorId: String,
        title: String,
        description: String,
        status: TodoStatus,
        completedAt: Instant?,
        plannedAt: Instant?,
    ): Todo {
        return jdbi.withExtension<Todo, TodoDao, Exception>(TodoDao::class.java) {
            it.updateTodo(
                todoId,
                authorId,
                title,
                description,
                status,
                completedAt,
                plannedAt
            )
        }
    }

    override fun findTodoById(todoId: UUID): Todo? {
        return jdbi.withExtension<Todo?, TodoDao, Exception>(TodoDao::class.java) {
            it.findTodoById(
                todoId,
            )
        }
    }
    override fun listTodosByAuthor(authorId: String): List<Todo> {
        return jdbi.withExtension<List<Todo>, TodoDao, Exception>(TodoDao::class.java) {
            it.listTodosByAuthor(
                authorId,
            )
        }
    }

    override fun deleteById(todoId: UUID): Boolean {
        return jdbi.withExtension<Boolean, TodoDao, Exception>(TodoDao::class.java) {
            it.deleteById(
                todoId,
            )
        }
    }

    override fun updateTodoStatus(todoId: UUID): Boolean {
        return jdbi.withExtension<Boolean, TodoDao, Exception>(TodoDao::class.java) {
            it.updateTodoStatus(
                todoId,
            )
        }
    }

    override fun markTodoAsCompleted(todoId: String, status: TodoStatus): Boolean {
        return jdbi.withExtension<Boolean, TodoDao, Exception>(TodoDao::class.java) {
            it.markTodoAsCompleted(
                todoId,
                status
            )
        }
    }

}