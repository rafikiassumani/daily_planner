package daily_planner.client

import com.google.inject.Guice
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.Request
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.server.metric.PrometheusExpositionService
import daily_planner.client.http.TodoController
import daily_planner.client.kafka.TodoKafkaProcessor
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Instant
import javax.inject.Inject

class Client @Inject constructor(
    private val kafkaProcessor: TodoKafkaProcessor,
    private val registry: PrometheusMeterRegistry,
    private val todoController: TodoController,
){

    private val healthEndpoint = { _: ServiceRequestContext, _: Request ->
        HttpResponse.ofJson(object {
            val message = "http client running ${Instant.now()}"
        })
    }
    fun processTodos() {
        kafkaProcessor.consume("todos")
    }

    fun buildServer(port: Int): Server {
        return Server.builder()
            .meterRegistry(registry)
            .http(port)
            .service("/", healthEndpoint)
            .service("/metrics", PrometheusExpositionService.of(registry.prometheusRegistry))
            .annotatedService(todoController)
            .decorator(MetricCollectingService.newDecorator(MeterIdPrefixFunction.ofDefault("daily_planner.http.service")))
            .build()
    }

}

fun main() {

    println("client class starting for processing messages")
    val injector = Guice.createInjector(
        ClientAppGuiceModule()
    )

    val client = injector.getInstance(Client::class.java)
    client.buildServer(8081)
        .start()
        .join()

    //client.processTodos()
}