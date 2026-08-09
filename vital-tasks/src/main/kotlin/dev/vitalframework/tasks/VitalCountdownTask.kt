package dev.vitalframework.tasks

import dev.vitalframework.BungeePlugin
import dev.vitalframework.BungeeRunnable
import dev.vitalframework.BungeeTask
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.SpigotRunnable
import dev.vitalframework.SpigotTask
import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalHasInfo
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.TimeUnit

/**
 * Defines a countdown task within the Vital framework.
 * A countdown task may perform an action that can end after a set countdown.
 *
 * By default, a countdown task will not be a Spring bean. That means that Vital will not automatically create an instance of it.
 * This is because countdown tasks are also meant to be used by vital-minigames, where each instanced-game may have multiple running countdowns that are not scoped to the entire server.
 *
 * A countdown task can be marked as "autoStart" to automatically start it when a new instance is created.
 * This functionality is disabled by default.
 *
 * ```java
 * @VitalCountdownTask.Info(interval = 1_000, countdown = 10, autoStart = false)
 * public class MyCountdownTask extends VitalCountdownTask.Spigot {
 *     public MyCountdownState(JavaPlugin plugin) {
 *         super(plugin);
 *     }
 *
 *     @Override
 *     public void onTick() {
 *         // ...
 *     }
 *
 *     @Override
 *     public void onExpire() {
 *         // ...
 *     }
 * }
 * ```
 */
abstract class VitalCountdownTask<P, R : Runnable, T>(
    val plugin: P,
) : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    private val logger = logger()

    /**
     * The current countdown of this task.
     */
    var countdown = getInfo(Info::class.java).countdown

    /**
     * Controls if this task can currently tick, decrease the [countdown] and call the [onTick] lifecycle function.
     */
    var allowTick = true

    /**
     * The internal task that runs to decrease the [countdown] and call the [onTick] lifecycle function.
     * This field will be `null` if this task is currently not running.
     */
    var runnable: R? = null
        private set

    /**
     * The internal task that's runs the [runnable] to decrease the [countdown] and call the [onTick] lifecycle function.
     * This field will be `null` if this task is currently not running.
     */
    var task: T? = null
        private set

    /**
     * Checks if this task is currently running.
     */
    val running
        get() = runnable != null && task != null

    init {
        val info = getInfo(Info::class.java)
        if (info.autoStart) {
            start()
        }
    }

    /**
     * Starts this task if It's not already running.
     * This function will call [createRunnable] and [createTask] to set up this task before starting the internal tick timer.
     * The [onStart] lifecycle function will be called after this task is ready for execution.
     *
     * On each tick, the [countdown] will be decreased and the [onTick] lifecycle function will be called.
     *
     * If this task is currently running, this function does nothing.
     */
    fun start() {
        val loggingContext = "Context: Vital countdown task '$this'."
        logger.debug("Countdown start was requested. $loggingContext")
        if (running) {
            logger.debug("Countdown is already running. $loggingContext")
            return
        }

        logger.debug("Creating runnable for countdown. $loggingContext")
        runnable = createRunnable()

        logger.debug("Creating task for countdown. $loggingContext")
        task = createTask()

        logger.debug("Calling 'onStart' lifecycle. $loggingContext")
        onStart()
    }

    /**
     * Stops this task if It's currently running.
     * This function stops and cleans up the internal [runnable] and [task] and then calls the [onStop] lifecycle function.
     *
     * If this task is currently not running, this function does nothing.
     */
    fun stop() {
        val loggingContext = "Context: Vital countdown task '$this'."
        logger.debug("Countdown stop was requested. $loggingContext")
        if (!running) {
            logger.debug("Countdown is already stopped. $loggingContext")
            return
        }

        logger.debug("Cancelling runnable for countdown. $loggingContext")
        cancelRunnable()

        logger.debug("Cancelling task for countdown. $loggingContext")
        cancelTask()
        runnable = null
        task = null

        logger.debug("Calling 'onStop' lifecycle. $loggingContext")
        onStop()
    }

    /**
     * Resets this task by setting the [countdown] to its initial value defined in [Info] and calls the [onReset] lifecycle function.
     */
    fun reset() {
        logger.debug("Resetting countdown for countdown task '$this' and calling 'onReset' lifecycle.")
        countdown = getInfo(Info::class.java).countdown
        onReset()
    }

    /**
     * Restarts this task by stopping it via [stop], resetting it's [countdown] via [reset],
     * starting the task again via [start] and finally calling the [onRestart] lifecycle function.
     */
    fun restart() {
        val loggingContext = "Context: Vital countdown task '$this'."
        logger.debug("Countdown restart was requested. $loggingContext")
        stop()
        reset()
        start()

        logger.debug("Calling 'onRestart' lifecycle. $loggingContext")
        onRestart()
    }

    /**
     * Handles a single tick of this task, decreases the [countdown] and calls the [onTick] lifecycle function.
     *
     * If the countdown reaches `0` this function will stop this task via [stop] and call the [onExpire] lifecycle function.
     *
     * If ticks are disabled by [allowTick], this function does nothing.
     */
    fun handleTick() {
        val loggingContext = "Context: Vital countdown task '$this'."
        logger.debug("Handling countdown tick. $loggingContext")
        if (!allowTick) {
            logger.debug("Task doesnt allow ticks. $loggingContext")
            return
        }

        logger.debug("Task allows ticks, checking against current countdown. $loggingContext")

        if (countdown <= 0) {
            logger.debug("Countdown is expired, stopping task. $loggingContext")
            stop()

            logger.debug("Countdown task was stopped, calling 'onExpire' lifecycle. $loggingContext")
            onExpire()
            return
        }

        logger.debug("Countdown has not yet expired, calling 'onTick' lifecycle. $loggingContext")
        onTick()
        countdown -= 1
    }

    /**
     * Creates the internal [runnable] for this task.
     */
    abstract fun createRunnable(): R

    /**
     * Creates the internal [runnable] for this task.
     */
    abstract fun createTask(): T

    /**
     * Cancels the internal [runnable] for this task.
     */
    abstract fun cancelRunnable()

    /**
     * Cancels the internal [task] for this task.
     */
    abstract fun cancelTask()

    /**
     * Lifecycle function; called when this task is started.
     */
    open fun onStart() {
        logger.debug("Lifecycle function 'onStart' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is ticked.
     * This task will only tick if [allowTick] is `true`.
     */
    open fun onTick() {
        logger.debug("Lifecycle function 'onTick' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is stopped.
     */
    open fun onStop() {
        logger.debug("Lifecycle function 'onStop' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Lifecycle function; called when this task's [countdown] reaches `0`.
     */
    open fun onExpire() {
        logger.debug("Lifecycle function 'onExpire' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is reset via [reset].
     */
    open fun onReset() {
        logger.debug("Lifecycle function 'onReset' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is restarted via [restart].
     */
    open fun onRestart() {
        logger.debug("Lifecycle function 'onRestart' was not overridden by Vital countdown task '$this'.")
    }

    /**
     * Defines the info for a [VitalCountdownTask].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val countdown: Long,
        val interval: Long = 1_000L,
        val autoStart: Boolean = false,
    )

    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalCountdownTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() =
            object : SpigotRunnable() {
                override fun run() = handleTick()
            }

        override fun createTask(): SpigotTask {
            val info = getInfo(Info::class.java)
            return runnable!!.runTaskTimer(plugin, 0L, ((info.interval / 1000.0) * 20L).toLong())
        }

        override fun cancelRunnable() {
            runnable?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }

    open class Bungee(
        plugin: BungeePlugin,
    ) : VitalCountdownTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
        override fun createRunnable() = BungeeRunnable { handleTick() }

        override fun createTask(): BungeeTask {
            val info = getInfo(Info::class.java)
            return ProxyServer.getInstance().scheduler.schedule(plugin, runnable, 0L, info.interval, TimeUnit.MILLISECONDS)!!
        }

        override fun cancelRunnable() {
            task?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }
}
