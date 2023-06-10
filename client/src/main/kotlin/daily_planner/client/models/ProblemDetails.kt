package daily_planner.client.models

data class ProblemDetails(
    val message: String,
    val status: Int,
    val traceId: String,
    val errors: MutableMap<String, String>
): Problem