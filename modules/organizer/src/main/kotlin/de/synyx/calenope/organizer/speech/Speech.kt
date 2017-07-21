package de.synyx.calenope.organizer.speech

/**
 * @author clausen - clausen@synyx.de
 */
interface Speech {

    fun ask (prompt : String, callback : (String) -> Unit)

}