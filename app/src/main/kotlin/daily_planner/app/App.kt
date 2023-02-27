/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package daily_planner.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.Request
import com.linecorp.armeria.common.grpc.GrpcMeterIdPrefixFunction
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.JacksonRequestConverterFunction
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.server.metric.PrometheusExpositionService
import daily_planner.app.grpc.TodoService
import daily_planner.app.grpc.UserService
import daily_planner.app.http.TodoController
import io.github.oshai.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.sql.Timestamp
import java.util.*


class App {
    private val healthEndpoint = { _: ServiceRequestContext, _: Request ->
        HttpResponse.of("http server running ${Timestamp(Date().time)}")
    }

    fun buildServer(port: Int): Server {
        return Server.builder()
            .meterRegistry(registry)
            .http(port)
            .service("/", healthEndpoint)
            .service("/metrics", PrometheusExpositionService.of(registry.prometheusRegistry))
            .annotatedService(TodoController(registry),
                JacksonRequestConverterFunction(ObjectMapper().registerKotlinModule())
            )
            .decorator(MetricCollectingService.newDecorator(MeterIdPrefixFunction.ofDefault("daily_planner.http.service")))
            .service(GrpcService.builder().addService(TodoService(registry)).build(),
                MetricCollectingService.newDecorator(GrpcMeterIdPrefixFunction.of("daily_planner.grpc.service")),
            )
            .service(GrpcService.builder().addService(UserService(registry)).build(),
                MetricCollectingService.newDecorator(GrpcMeterIdPrefixFunction.of("daily_planner.grpc.service")))
            .build()
    }

    companion object {
        val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}

fun main() {
    val logger = KotlinLogging.logger {}

    val app = App()
    val server = app.buildServer(8080)
    server.closeOnJvmShutdown()
    server.start().join()
    logger.info("server has started. serving request on port ${server.activeLocalPort()}")
}
