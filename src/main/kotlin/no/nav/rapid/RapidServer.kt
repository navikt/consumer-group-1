package no.nav.rapid

import io.ktor.server.engine.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.quizrapid.QuizParticipant
import no.nav.quizrapid.QuizRapid
import no.nav.quizrapid.nooParticipant
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.LoggerFactory


class RapidServer(
    config: Config,
    builder: (String, () -> Boolean) -> ApplicationEngine,
    participant: QuizParticipant = nooParticipant,
    run: QuizRapid.(records: ConsumerRecords<String, String>) -> Unit = {}
) {

    private val logger = LoggerFactory.getLogger(config.appName)
    private val quizRapid = QuizRapid(
        config,
        rapidTopic = config.quizTopic,
        participant = participant,
        run = run
    )
    private val ktor = builder(config.appName, quizRapid::isRunning)

    fun startBlocking() {
        runBlocking { start() }
    }

    suspend fun start() {
        val ktorServer = ktor.start(false)
        try {
            coroutineScope {
                launch { quizRapid.start() }
            }

        } finally {
            val gracePeriod = 5000L
            val forcefulShutdownTimeout = 30000L
            logger.info("shutting down ktor, waiting $gracePeriod ms for workers to exit. Forcing shutdown after $forcefulShutdownTimeout ms")
            ktorServer.stop(gracePeriod, forcefulShutdownTimeout)
            logger.info("ktor shutdown complete: end of life. goodbye.")
        }
    }
}