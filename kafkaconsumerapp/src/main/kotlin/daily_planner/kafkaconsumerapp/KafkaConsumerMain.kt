package daily_planner.kafkaconsumerapp

import com.google.inject.Guice
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.Request
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.server.metric.PrometheusExpositionService
import daily_planner.kafkaconsumerapp.kafka.TodoKafkaProcessor
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Instant
import javax.inject.Inject

class KafkaConsumerMain @Inject constructor(
    private val kafkaProcessor: TodoKafkaProcessor,
    private val registry: PrometheusMeterRegistry,
){

    private val healthEndpoint = { _: ServiceRequestContext, _: Request ->
        HttpResponse.ofJson(object {
            val message = "kafka consumer app running ${Instant.now()}"
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
            .decorator(MetricCollectingService.newDecorator(MeterIdPrefixFunction.ofDefault("daily_planner.kafkaconsumerapp.service")))
            .build()
    }

}

fun main() {

    println("kafka consumer app starting for processing messages")
    val injector = Guice.createInjector(
        KafkaConsumerAppGuiceModule()
    )

    val client = injector.getInstance(KafkaConsumerMain::class.java)
    client.buildServer(8087)
        .start()
        .join()

    client.processTodos()
}