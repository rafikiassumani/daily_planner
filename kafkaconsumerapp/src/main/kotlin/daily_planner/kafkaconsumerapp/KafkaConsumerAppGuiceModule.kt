package daily_planner.kafkaconsumerapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import javax.inject.Qualifier
import javax.inject.Singleton

class KafkaConsumerAppGuiceModule : AbstractModule() {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.VALUE_PARAMETER
    )
    annotation class KafkaBrokers

    @Provides
    @KafkaBrokers
    fun providesKafkaBrokers(): String {
        return "localhost:9092"
    }

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
    @Singleton
    fun providesPrometheusRegistry() : PrometheusMeterRegistry {
        return PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Provides
    fun providesLogger() : KLogger {
        return KotlinLogging.logger {}
    }
}