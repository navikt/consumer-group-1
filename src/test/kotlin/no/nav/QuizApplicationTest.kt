package no.nav

import no.nav.rapid.Answer
import no.nav.rapid.Question
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class QuizApplicationTest {


    @Test
    fun `håndterer team-registration`() {
        val teamNavn = "consumer-group-1"
        val qa = QuizApplication(teamNavn)
        qa.handle(Question(category = "team-registration", question =  "register new team..."))
        val messages = qa.messages()
        assertEquals(1, messages.size)
        assertEquals(teamNavn, (messages[0] as Answer).answer)
    }
    @Test
    fun `håndterer arithmetics`() {
        val teamNavn = "consumer-group-1"
        val qa = QuizApplication(teamNavn)
        qa.handle(Question(category = "arithmetic", question =  "5 - 3"))
        val messages = qa.messages()
        assertEquals(1, messages.size)
        assertEquals((messages[0] as Answer).answer, "2")
    }

    @Test
    fun `håndterer make-ingress`() {
        val teamNavn = "consumer-group-1"
        val qa = QuizApplication(teamNavn)
        qa.handle(Question(category = "make-ingress", question =  "[NAIS Oppgave] Lag en NAIS ingress for appen din i formatet: <app navn>.dev.intern.nav.no. Send link til ingress som svar"))
        val messages = qa.messages()
        assertEquals(1, messages.size)
        assertEquals((messages[0] as Answer).answer, "https://consumer-group-1.dev.intern.nav.no/")
    }

    @Test
    fun `håndterer NAV-questions`() {
        val teamNavn = "consumer-group-1"
        val qa = QuizApplication(teamNavn)
        qa.handle(Question(category = "NAV", question =  "På hvilken nettside finner man informasjon om rekruttering til NAV IT?"))
        val messages = qa.messages()
        assertEquals(1, messages.size)
        assertEquals((messages[0] as Answer).answer, "https://www.detsombetyrnoe.no/")
    }
}