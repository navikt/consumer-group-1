package no.nav

import no.nav.rapid.Answer
import no.nav.rapid.Question
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class QuizApplicationTest {


    @Test
    fun `håndterer team-registration`() {
        val teamNavn = "mitt-team-navn"
        val qa = QuizApplication(teamNavn)
        qa.handle(Question(category = "team-registration", question =  "register new team..."))
        val messages = qa.messages()
        assertEquals(1, messages.size)
        assertEquals(teamNavn, (messages[0] as Answer).answer)
    }

}