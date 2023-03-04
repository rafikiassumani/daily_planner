package daily_planner.client.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import daily_planner.stubs.Todo
import io.github.oshai.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

class TodoKafkaProcessor(private val brokers: String){

    //Need single jsonMapper
    private val jsonMapper = ObjectMapper().apply {
        registerKotlinModule()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        dateFormat = StdDateFormat()
    }

    private val logger = KotlinLogging.logger {}
    private fun crateConsumer() : KafkaConsumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "todo-processor"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        return KafkaConsumer(props)
    }

    fun consume(topic: String) {
       val consumer = crateConsumer()
       consumer.subscribe(listOf(topic))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(10))

            // Trigger cadence workflow and pass it the todos
            // send email with created todo,
            // Create report for todos that are in-progress, completed
            println(records)
            records.forEach {
                val todoObject = jsonMapper.readValue(it.value(), Todo::class.java)
                logger.info("======= Start logging todo from kafka consumer ======")
                logger.info(todoObject.id)
                logger.info(todoObject.title)
                logger.info(todoObject.description)
                logger.info(todoObject.createdAt.toString())
                logger.info(todoObject.status.toString())
                logger.info("======= End logging todo from kafka consumer ======")
            }
        }
    }
}