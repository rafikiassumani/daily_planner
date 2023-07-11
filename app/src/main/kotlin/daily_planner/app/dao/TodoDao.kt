package daily_planner.app.dao

import daily_planner.stubs.Todo
import daily_planner.stubs.TodoStatus
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlScript
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.Instant
import java.util.*

interface TodoDao {
    @SqlScript(
        """CREATE EXTENSION IF NOT EXISTS "uuid-ossp""""
    )

    @SqlScript("""CREATE TYPE todo_status AS ENUM ('CREATED', 'IN_PROGRESS', 'COMPLETED')""")
    @SqlScript(
        """ 
        CREATE TABLE IF NOT EXISTS todo (
            todo_id uuid DEFAULT uuid_generate_v4(), 
            title VARCHAR(255) NOT NULL, 
            description VARCHAR NOT NULL, 
            author_id VARCHAR(255) NOT NULL, 
            status todo_status NOT NULL,
            completed_at TIMESTAMP,
            planned_at TIMESTAMP,
            updated_at TIMESTAMP,
            created_at TIMESTAMP,
            PRIMARY KEY (todo_id)
         )
    """
    )
    fun createTable()

    @SqlUpdate(""" 
        INSERT INTO todo (
            title, 
            description, 
            author_id, 
            status, 
            completed_at,
            planned_at,
            updated_at,
            created_at
        ) VALUES (
            :title, 
            :description, 
            :authorId, 
            :status, 
            :completedAt, 
            :plannedAt, 
            :updatedAt,
            now() 
        )
    """)
    @GetGeneratedKeys
    @RegisterBeanMapper(Todo::class)
    fun createTodo(
        title: String,
        description: String,
        authorId: String?,
        status: TodoStatus,
        completedAt: Instant? = null,
        plannedAt: Instant?,
        updatedAt: Instant? = null,
    ) : Todo

    @SqlUpdate(""" 
        UPDATE todo SET
            author_id = :authorId,
            title = :title, 
            description = :description,
            status = :status,
            completed_at = :completedAt,
            planned_at = :plannedAt,
            updated_at = now()
        WHERE todo_id = :todoId
    """)
    @GetGeneratedKeys
    @RegisterBeanMapper(Todo::class)
    fun updateTodo(
        todoId: UUID,
        authorId: String,
        title: String,
        description: String,
        status: TodoStatus,
        completedAt: Instant?,
        plannedAt: Instant?,
    ): Todo

    @SqlQuery("SELECT * FROM todo where todo_id = :todoId")
    @RegisterBeanMapper(Todo::class)
    fun findTodoById(todoId: UUID): Todo?
    @SqlQuery("SELECT * FROM todo where author_id = :authorId")
    @RegisterBeanMapper(Todo::class)
    fun listTodosByAuthor(authorId: String): List<Todo>

    @SqlUpdate("DELETE FROM todo where todo_id = :todoId")
    fun deleteById(todoId: UUID): Boolean

    @SqlUpdate(""" 
        UPDATE todo SET
            status = :status,
            updated_at = now()
        WHERE todo_id = :todoId
    """)
    fun updateTodoStatus(todoId: UUID): Boolean

    @SqlUpdate(""" 
        UPDATE todo SET
            status = :status,
            completed_at = now(),
            updated_at = now()
        WHERE todo_id = :todoId
    """)
    fun markTodoAsCompleted(
        todoId: String,
        status: TodoStatus,
    ): Boolean
}