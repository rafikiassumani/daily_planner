package daily_planner.stubs

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor
import java.util.Date

data class Todo(
    var todoId: String,
    var title: String,
    var description: String,
    var authorId: String,
    var plannedAt: Date? = null,
    var completedAt: Date? = null,
    var updatedAt: Date? = null,
    var createdAt: Date? = null,
    //need to fix this
    var status: Int = TodoStatus.CREATED.ordinal
) {
    @JdbiConstructor constructor() : this("", "", "", "")
}

enum class TodoStatus {
    CREATED, IN_PROGRESS, COMPLETED
}
