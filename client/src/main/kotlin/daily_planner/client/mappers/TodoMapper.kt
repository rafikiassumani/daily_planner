package daily_planner.client.mappers

import daily_planner.client.utils.DateUtil
import daily_planner.stubs.Todo
import daily_planner.stubs.TodoStatus
import todo.app.grpc.TodoOuterClass
import java.time.Instant

class TodoMapper {
    fun mapTodo(todo: TodoOuterClass.Todo): Todo {
        return Todo().apply {
            todoId = todo.todoId
            title = todo.title
            description = todo.description
            authorId = todo.authorId
            plannedAt = DateUtil.convertProtoToInstance(todo.plannedAt)
            completedAt = DateUtil.convertProtoToInstance(todo.completedAt)
            updatedAt = DateUtil.convertProtoToInstance(todo.updatedAt)
            createdAt = DateUtil.convertProtoToInstance(todo.createdAt)
            status = TodoStatus.valueOf(todo.status.name)
        }
    }
}