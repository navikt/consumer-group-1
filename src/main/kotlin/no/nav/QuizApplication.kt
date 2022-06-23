package no.nav

import no.nav.db.Database
import no.nav.quizrapid.*
import no.nav.rapid.Assessment
import no.nav.rapid.Question


/**
 * QuizApplication
 *
 * Her skal teamet bygge ut funksjonalitet for å løse oppgavene i leesah-game.
 */
class QuizApplication(private val teamName: String, database: Database? = null): QuizParticipant(teamName) {

    override fun handle(question: Question) {
        logger.log(question)
        if (question.category == "team-registration") handleRegisterTeam(question);
        if (question.category == "arithmetic") handleArithmetic(question);
        if (question.category == "NAV") handleQuestion(question);
        if(question.category == "make-ingress") handleIngress(question);
    }


    override fun handle(assessment: Assessment) {
        logger.log(assessment)
    }

    /**
     * Spørsmål handlers
     */

    private fun handleRegisterTeam(question: Question) {
        answer(question.category, question.id(), "consumer-group-1")
    }

    private fun handleQuestion(question: Question) {
        if(question.question == "På hvilken nettside finner man informasjon om rekruttering til NAV IT?") answer(question.category, question.id(), "https://www.detsombetyrnoe.no/");
        if(question.question == "Hva heter applikasjonsplattformen til NAV?") answer(question.category, question.id(), "NAIS");

    }

    private fun handleIngress(question: Question) {
        answer(question.category, question.id(), "https://consumer-group-1.dev.intern.nav.no/")
    }

    private fun handleArithmetic(question: Question) {
        var expression = question.question.split(" ");

        when (expression[1]) {
            "-" -> {
                answer(question.category, question.id(), (expression[0].toInt() - expression[2].toInt()).toString())
            }
            "+" -> {
                answer(question.category, question.id(), (expression[0].toInt() + expression[2].toInt()).toString())
            }
            "*" -> {
                answer(question.category, question.id(), (expression[0].toInt() * expression[2].toInt()).toString())
            }
            "/" -> {
                answer(question.category, question.id(), (expression[0].toInt() / expression[2].toInt()).toString())
            }
        }
    }
}