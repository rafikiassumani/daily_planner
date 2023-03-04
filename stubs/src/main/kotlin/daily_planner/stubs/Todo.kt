package daily_planner.stubs

data class Todo(
    var id: String?,
    var title: String,
    var description: String,
    var createdAt: Long? = null,
    var modifiedAt: Long? = null,
    //need to fix this
    var status: String = TodoStatus.CREATED.toString()
)

enum class TodoStatus {
    CREATED, IN_PROGRESS, COMPLETED
}
