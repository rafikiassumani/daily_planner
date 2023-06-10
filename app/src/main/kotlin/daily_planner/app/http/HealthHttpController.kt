package daily_planner.app.http

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.annotation.Get
import java.time.Instant
import java.util.*

class HealthHttpController {

    @Get("/")
    fun health(): HttpResponse {
        return HttpResponse.of("http server running ${Instant.now()}")
    }
}