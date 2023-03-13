/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package daily_planner.app

import com.google.inject.Guice
import com.google.inject.Injector
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.Request
import com.linecorp.armeria.common.grpc.GrpcMeterIdPrefixFunction
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.server.metric.PrometheusExpositionService
import daily_planner.app.grpc.TodoService
import daily_planner.app.grpc.UserService
import daily_planner.app.http.TodoController
import io.github.oshai.KotlinLogging
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject


class App @Inject constructor(
    private val registry: PrometheusMeterRegistry,
    private val todoController: TodoController,
    private val todoGrpcService: TodoService,
    private val userGrpcService: UserService
) {
    private val healthEndpoint = { _: ServiceRequestContext, _: Request ->
        HttpResponse.of("http server running ${Timestamp(Date().time)}")
    }

    fun buildServer(port: Int): Server {
        return Server.builder()
            .meterRegistry(registry)
            .http(port)
            .service("/", healthEndpoint)
            .service("/metrics", PrometheusExpositionService.of(registry.prometheusRegistry))
            .annotatedService(todoController)
            .decorator(MetricCollectingService.newDecorator(MeterIdPrefixFunction.ofDefault("daily_planner.http.service")))
            .service(
                GrpcService.builder()
                .addService(todoGrpcService)
                .addService(userGrpcService)
                .build(),
                MetricCollectingService.newDecorator(GrpcMeterIdPrefixFunction.of("daily_planner.grpc.service")),
            )
            .build()
    }

}


fun main() {
    val logger = KotlinLogging.logger {}

    val injector: Injector = Guice.createInjector(
        AppGuiceModule()
    )
    val app = injector.getInstance(App::class.java)

    //val app = App()
    val server = app.buildServer(8080)
    server.closeOnJvmShutdown()
    server.start().join()
    logger.info("server has started. serving request on port ${server.activeLocalPort()}")
}
