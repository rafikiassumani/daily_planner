package daily_planner.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import javax.inject.Qualifier
import javax.inject.Singleton

class AppGuiceModule : AbstractModule() {

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
        return  ObjectMapper().apply {
            registerKotlinModule()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            dateFormat = StdDateFormat()
        }
    }

    @Provides
    @Singleton
    fun providesPrometheusRegistry() : PrometheusMeterRegistry {
        return PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Provides
    @KafkaBrokers
    fun providesKafkaBrokers(): String {
        return "localhost:9092"
    }

    @Provides
    fun providesLogger() : KLogger {
        return KotlinLogging.logger {}
    }

}