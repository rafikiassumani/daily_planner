package daily_planner.client

import com.google.inject.Guice
import daily_planner.client.kafka.TodoKafkaProcessor
import javax.inject.Inject

class Client @Inject constructor(
    private val kafkaProcessor: TodoKafkaProcessor
){

    fun processTodos() {
        kafkaProcessor.consume("todos")
    }

}

fun main() {

    println("client class starting for processing messages")
    val injector = Guice.createInjector(
        ClientAppGuiceModule()
    )

    val client = injector.getInstance(Client::class.java)
    client.processTodos()
}