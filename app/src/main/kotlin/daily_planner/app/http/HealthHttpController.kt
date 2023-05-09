package daily_planner.app.http

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.annotation.Get
import java.sql.Timestamp
import java.util.*

class HealthHttpController {

    @Get("/")
    fun health(): HttpResponse {
        return HttpResponse.of("http server running ${Timestamp(Date().time)}")
    }
}