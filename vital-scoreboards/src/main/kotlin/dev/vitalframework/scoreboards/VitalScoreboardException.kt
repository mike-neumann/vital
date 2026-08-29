package dev.vitalframework.scoreboards

/**
 * Internal exception; thrown during [VitalScoreboard] lifecycles.
 */
abstract class VitalScoreboardException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown if an exception occurs while updating a [VitalScoreboard].
     */
    class Update(
        cause: Throwable,
    ) : VitalScoreboardException("Error while updating scoreboard. You might be trying to access data that doesnt exist anymore?", cause)
}
