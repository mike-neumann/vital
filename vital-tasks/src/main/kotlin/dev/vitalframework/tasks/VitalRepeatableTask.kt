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
 * Defines a repeatable task within the Vital framework.
 * A task may perform repeated actions with a set interval.
 *
 * By default, a repeatable task will not be a Spring bean. That means that Vital will not automatically create an instance of it.
 * This is because repeatable tasks are also meant to be used by vital-minigames, where each instanced-game may have multiple running tasks that are not scoped to the entire server.
 *
 * A repeatable task can be marked as "autoStart" to automatically start it when a new instance is created.
 * This functionality is disabled by default.
 *
 * ```java
 * @VitalRepeatableTask.Info(interval = 1_000, autoStart = false)
 * public class MyRepeatableTask extends VitalRepeatableTask.Spigot {
 *     public MyRepeatableTask(JavaPlugin plugin) {
 *         super(plugin);
 *     }
 *
 *     @Override
 *     public void onTick() {
 *         // ...
 *     }
 * }
 * ```
 */
abstract class VitalRepeatableTask<P, R : Runnable, T>(
    val plugin: P,
) : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    private val logger = logger()

    /**
     * Controls if this task can currently tick and call the [onTick] lifecycle function.
     */
    var allowTick = true

    /**
     * The internal task that runs to call the [onTick] lifecycle function.
     * This field will be `null` if this task is currently not running.
     */
    var runnable: R? = null
        private set

    /**
     * The internal task that's runs the [runnable] to call the [onTick] lifecycle function.
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
     * On each tick, the [onTick] lifecycle function will be called.
     *
     * If this task is currently running, this function does nothing.
     */
    fun start() {
        val loggingContext = "Context: Vital repeatable task '$this'."
        logger.debug("Task start was requested. $loggingContext")
        if (running) {
            logger.debug("Task is already running. $loggingContext")
            return
        }

        logger.debug("Creating runnable for task start. $loggingContext")
        runnable = createRunnable()
        logger.debug("Creating task for task start. $loggingContext")
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
        val loggingContext = "Context: Vital repeatable task '$this'."
        logger.debug("Task stop was requested. $loggingContext")
        if (!running) {
            logger.debug("Task is already stopped. $loggingContext")
            return
        }

        logger.debug("Cancelling runnable for task stop. $loggingContext")
        cancelRunnable()

        logger.debug("Cancelling task for task stop. $loggingContext")
        cancelTask()
        runnable = null
        task = null

        logger.debug("Task is stopped, calling 'onStop' lifecycle. $loggingContext")
        onStop()
    }

    /**
     * Handles a single tick of this task and calls the [onTick] lifecycle function.
     * If ticks are disabled by [allowTick], this function does nothing.
     */
    fun handleTick() {
        val loggingContext = "Context: Vital repeatable task '$this'."
        logger.debug("Handling tick. $loggingContext")
        if (!allowTick) {
            logger.debug("Task doesnt allow ticks. $loggingContext")
            return
        }

        logger.debug("Calling 'onTick' lifecycle. $loggingContext")
        onTick()
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
        logger.debug("Lifecycle function 'onStart' was not overridden by Vital repeatable task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is ticked.
     * This task will only tick if [allowTick] is `true`.
     */
    open fun onTick() {
        logger.debug("Lifecycle function 'onTick' was not overridden by Vital repeatable task '$this'.")
    }

    /**
     * Lifecycle function; called when this task is stopped.
     */
    open fun onStop() {
        logger.debug("Lifecycle function 'onStop' was not overridden by Vital repeatable task '$this'.")
    }

    /**
     * Defines the info for a [VitalRepeatableTask].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val interval: Long,
        val autoStart: Boolean = false,
    )

    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalRepeatableTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() =
            object : SpigotRunnable() {
                override fun run() = handleTick()
            }

        override fun createTask(): SpigotTask {
            val info = getInfo(Info::class.java)
            return runnable!!.runTaskTimer(plugin, 0L, ((info.interval.toFloat() / 1_000f) * 20f).toLong())
        }

        override fun cancelRunnable() {
            runnable?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }

    class Bungee(
        plugin: BungeePlugin,
    ) : VitalRepeatableTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
        override fun createRunnable() = BungeeRunnable { handleTick() }

        override fun createTask(): BungeeTask {
            val info = getInfo(Info::class.java)
            return ProxyServer.getInstance().scheduler.schedule(
                plugin,
                runnable,
                0L,
                info.interval,
                TimeUnit.MILLISECONDS,
            )!!
        }

        override fun cancelRunnable() {
            task?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }
}
