package daily_planner.client

import arrow.integrations.jackson.module.EitherModuleConfig
import arrow.integrations.jackson.module.IorModuleConfig
import arrow.integrations.jackson.module.ValidatedModuleConfig
import arrow.integrations.jackson.module.registerArrowModule
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import javax.inject.Singleton

class ClientAppGuiceModule : AbstractModule() {

    @Provides
    @Singleton
    fun providesObjectMapper(): ObjectMapper {
      return ObjectMapper()
          .registerKotlinModule()
          .registerModule(JavaTimeModule())
          .registerArrowModule(
              eitherModuleConfig = EitherModuleConfig("left", "right"),           // sets the field names for either left / right
              validatedModuleConfig = ValidatedModuleConfig("invalid", "valid"),  // sets the field names for validated invalid / valid
              iorModuleConfig = IorModuleConfig("left", "right")                  // sets the field names for ior left / right
          )
          .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)            // do not serialize None as nulls
          .setDateFormat(StdDateFormat())
          //.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }


    @Provides
    @Singleton
    fun provideRpcClient(): TodoServiceGrpcKt.TodoServiceCoroutineStub {
        return GrpcClients.newClient(
            //need to fix this url as well
            "gproto+http://grpc-app:8080/",
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