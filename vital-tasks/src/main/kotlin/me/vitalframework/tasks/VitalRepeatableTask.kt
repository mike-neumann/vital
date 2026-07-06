package me.vitalframework.tasks

import me.vitalframework.BungeePlugin
import me.vitalframework.BungeeRunnable
import me.vitalframework.BungeeTask
import me.vitalframework.SpigotPlugin
import me.vitalframework.SpigotRunnable
import me.vitalframework.SpigotTask
import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalHasInfo
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.TimeUnit

/**
 * Defines a repeatable task within the Vital framework.
 * A task may perform repeated actions with a set interval.
 *
 * ```java
 * @VitalRepeatableTask.Info(interval = 1_000)
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
        if (running) {
            return
        }

        runnable = createRunnable()
        task = createTask()
        onStart()
    }

    /**
     * Stops this task if It's currently running.
     * This function stops and cleans up the internal [runnable] and [task] and then calls the [onStop] lifecycle function.
     *
     * If this task is currently not running, this function does nothing.
     */
    fun stop() {
        if (!running) {
            return
        }

        cancelRunnable()
        cancelTask()
        runnable = null
        task = null
        onStop()
    }

    /**
     * Handles a single tick of this task and calls the [onTick] lifecycle function.
     * If ticks are disabled by [allowTick], this function does nothing.
     */
    fun handleTick() {
        if (!allowTick) {
            return
        }

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
    open fun onStart() {}

    /**
     * Lifecycle function; called when this task is ticked.
     * This task will only tick if [allowTick] is `true`.
     */
    open fun onTick() {}

    /**
     * Lifecycle function; called when this task is stopped.
     */
    open fun onStop() {}

    /**
     * Defines the info for a [VitalRepeatableTask].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val interval: Long,
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
