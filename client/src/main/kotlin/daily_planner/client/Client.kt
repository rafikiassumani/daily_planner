package daily_planner.client

import daily_planner.client.kafka.TodoKafkaProcessor
import org.apache.kafka.clients.consumer.KafkaConsumer

class Client {
    companion object {
        val kafkaProcessor = TodoKafkaProcessor("localhost:9092")
    }

}

fun main() {
    println("client class started $Client.brokers")
    Client.kafkaProcessor.consume("todos")
}