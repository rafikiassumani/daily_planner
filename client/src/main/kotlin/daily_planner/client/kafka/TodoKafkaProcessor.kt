package daily_planner.client.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import daily_planner.client.ClientAppGuiceModule.KafkaBrokers
import daily_planner.stubs.TodoEvent
import io.github.oshai.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import javax.inject.Inject

class TodoKafkaProcessor @Inject constructor(
    @KafkaBrokers private val brokers: String,
    private val jsonMapper: ObjectMapper
){

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
                val todoObject = jsonMapper.readValue(it.value(), TodoEvent::class.java)
                logger.info("======= Start logging todo from kafka consumer ======")
                logger.info(todoObject.eventType)
                logger.info(todoObject.todoData.id)
                logger.info(todoObject.todoData.title)
                logger.info(todoObject.todoData.description)
                logger.info(todoObject.todoData.createdAt.toString())
                logger.info(todoObject.todoData.status)
                logger.info("======= End logging todo from kafka consumer ======")
            }
        }
    }
}