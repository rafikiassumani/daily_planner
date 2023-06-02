package daily_planner.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.linecorp.armeria.client.grpc.GrpcClients
import daily_planner.client.services.TodoServiceClient
import daily_planner.client.services.TodoServiceClientImpl
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import todo.app.grpc.TodoServiceGrpcKt
import javax.inject.Qualifier
import javax.inject.Singleton

class ClientAppGuiceModule : AbstractModule() {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER
    )
    annotation class KafkaBrokers

    @Provides
    @Singleton
    fun providesObjectMapper(): ObjectMapper {
      return ObjectMapper()
          .registerKotlinModule()
          .registerModule(JavaTimeModule())
          .setDateFormat(StdDateFormat())
          //.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
    @Provides
    @KafkaBrokers
    fun providesKafkaBrokers(): String {
        return "localhost:9092"
    }

    @Provides
    @Singleton
    fun provideRpcClient(): TodoServiceGrpcKt.TodoServiceCoroutineStub {
        return GrpcClients.newClient(
            "gproto+http://127.0.0.1:8080/",
            TodoServiceGrpcKt.TodoServiceCoroutineStub::class.java
        )
    }

    @Provides
    @Singleton
    fun provideTodoServiceClient(): TodoServiceClient {
       return TodoServiceClientImpl(provideRpcClient())
    }

    @Provides
    @Singleton
    fun providesPrometheusRegistry() : PrometheusMeterRegistry {
        return PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Provides
    fun providesLogger() : KLogger {
        return KotlinLogging.logger {}
    }
}