package daily_planner.client.services

import daily_planner.stubs.Todo
import kotlinx.coroutines.flow.Flow
import todo.app.grpc.TodoOuterClass

interface TodoServiceClient {
   suspend fun createTodo(todo: Todo): TodoOuterClass.ID
   suspend fun getTodo(todoUUID: String): TodoOuterClass.Todo
   suspend fun updateTodo(todo: Todo): TodoOuterClass.ID
   suspend fun deleteTodo(todoUUID: String): TodoOuterClass.ID
   fun getAllTodosByAuthor(authorId: String): Flow<TodoOuterClass.Todo>

}