package no.nav.quizrapid


import com.fasterxml.jackson.core.JacksonException
import no.nav.rapid.*
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


abstract class QuizParticipant(private val teamName: String) {
    private val outBox = mutableListOf<Answer>()

    open fun handle(question: Question) {}
    open fun handle(answer: Answer) {}
    open fun handle(assessment: Assessment) {}

    protected fun answer(category: String, questionId: String, answer: String) {
        outBox.add(Answer(category = category, questionId = questionId, teamName = teamName, answer = answer))
    }

    fun messages(): List<Message> = outBox.toList().also { outBox.clear() }
}

val nooParticipant = object : QuizParticipant("noop") {

    override fun handle(question: Question){}

    override fun handle(answer: Answer){}

    override fun handle(assessment: Assessment){}

}

class QuizRapid(
    private val config: Config,
    clientId: String = UUID.randomUUID().toString().slice(1..5),
    private val rapidTopic: String = "quiz-rapid",
    private val participant: QuizParticipant = nooParticipant,
    private val run: QuizRapid.(records: ConsumerRecords<String, String>) -> Unit = {}
) {
    private val consumer = KafkaConsumer(config.consumerConfig(clientId, config.consumerGroup, config.autoCommit), StringDeserializer(), StringDeserializer())
    private val producer = KafkaProducer(config.producerConfig(clientId), StringSerializer(), StringSerializer())
    private val logger = LoggerFactory.getLogger(QuizRapid::class.java)

    private val running = AtomicBoolean(false)

    internal fun isRunning() = running.get()

    internal fun publish(message: String) {
        //check(!producerClosed.get()) { "can't publish messages when producer is closed" }
        producer.send(ProducerRecord(rapidTopic, message)) { _, err ->
            if (err == null || !isFatalError(err)) return@send
            logger.error("Shutting down QuizRapid due to fatal error: ${err.message}", err)
            stop()
        }
        producer.flush()
    }

    fun start() {
        logger.info("starting QuizRapid")
        if (running.getAndSet(true)) return logger.info("QuizRapid already started")
        consumeMessages()
    }

    fun stop() {
        logger.info("stopping QuizRapid")
        if (!running.getAndSet(false)) return logger.info("rapid already stopped")
        consumer.wakeup()
    }

    private fun consumeMessages() {
        var lastException: Exception? = null
        try {
            consumer.subscribe(listOf(rapidTopic))
            while (running.get()) {
                consumer.poll(Duration.ofSeconds(1)).also { records ->
                    records.forEach { participantHandle(it.value()) }
                    run(records)
                    participant.messages().forEach { publish(it.json()) }
                }
            }
        } catch (err: WakeupException) {
            // throw exception if we have not been told to stop
            if (running.get()) throw err
        } catch (err: Exception) {
            lastException = err
            throw err
        } finally {
            closeResources(lastException)
        }
    }

    private fun closeResources(lastException: Exception?) {
        if (running.getAndSet(false)) {
            logger.warn("stopped consuming messages due to an error", lastException)
        } else {
            logger.info("stopped consuming messages after receiving stop signal")
        }
        tryAndLog(producer::close)
        tryAndLog(consumer::unsubscribe)
        tryAndLog(consumer::close)
    }

    private fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (err: Exception) {
            logger.error(err.message, err)
        }
    }

    private fun participantHandle(message: String) {
        try {
            // ugly, I know
            tryFromRaw<Question>(message) {
                it.containsValue("type", MessageType.QUESTION.name)
            }?.also { return participant.handle(it) }
            tryFromRaw<Answer>(message) {
                it.containsValue("type", MessageType.ANSWER.name)
            }?.also { return participant.handle(it) }
            tryFromRaw<Assessment>(message) {
                it.containsValue("type", MessageType.ASSESSMENT.name)
            }?.also { if(it.teamName == config.appName) return participant.handle(it) }
        } catch (e: JacksonException) {
            logger.warn("failed to parse, skipping message = $message")
            logger.warn(e.toString())
        }
    }

    companion object {
        private fun isFatalError(err: Exception) = when (err) {
            is InvalidTopicException,
            is RecordBatchTooLargeException,
            is RecordTooLargeException,
            is UnknownServerException,
            is AuthorizationException -> true
            else -> false
        }
    }

}






