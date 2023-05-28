package daily_planner.app.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import daily_planner.app.AppGuiceModule.KafkaBrokers
import daily_planner.stubs.TodoEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import javax.inject.Inject

class TodoKafkaProducer @Inject constructor(
    @KafkaBrokers private val brokers: String,
    private val jsonMapper: ObjectMapper
) {

    private fun createProducer(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java.canonicalName
        props["value.serializer"] = StringSerializer::class.java.canonicalName
        return KafkaProducer(props)
    }

    suspend fun produce(topic: String, todo: TodoEvent) {
        withContext(Dispatchers.IO) {
            try {
                val todoJson = jsonMapper.writeValueAsString(todo)
                createProducer().send(ProducerRecord(topic, todoJson)).get()
            } catch (e: Exception) {
                println(e.stackTrace)
            }

        }
    }
}