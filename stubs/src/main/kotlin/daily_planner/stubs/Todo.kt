package daily_planner.stubs

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor
import java.time.Instant

data class Todo(
    var todoId: String = "",
    var title: String,
    var description: String,
    var authorId: String,
    var plannedAt: Instant? = null,
    var completedAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdAt: Instant? = null,
    var status: TodoStatus = TodoStatus.CREATED
) {
    @JdbiConstructor constructor() : this("", "", "", "")
}

enum class TodoStatus {
    CREATED, IN_PROGRESS, COMPLETED
}
