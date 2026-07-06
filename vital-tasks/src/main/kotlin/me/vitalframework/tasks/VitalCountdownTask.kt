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
 * Defines a countdown task within the Vital framework.
 * A countdown task may perform an action that can end after a set countdown.
 *
 * ```java
 * public class MyCountdownState extends VitalCountdownState.Spigot() {
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

    /**
     * The current countdown of this task.
     */
    var countdown = getInfo(Info::class.java).interval

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
     * Resets this task by setting the [countdown] to its initial value defined in [Info] and calls the [onReset] lifecycle function.
     */
    fun reset() {
        countdown = getInfo(Info::class.java).countdown
        onReset()
    }

    /**
     * Restarts this task by stopping it via [stop], resetting it's [countdown] via [reset],
     * starting the task again via [start] and finally calling the [onRestart] lifecycle function.
     */
    fun restart() {
        stop()
        reset()
        start()
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
        if (!allowTick) {
            return
        }

        if (countdown <= 0) {
            stop()
            onExpire()
            return
        }

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
     * Lifecycle function; called when this task's [countdown] reaches `0`.
     */
    open fun onExpire() {}

    /**
     * Lifecycle function; called when this task is reset via [reset].
     */
    open fun onReset() {}

    /**
     * Lifecycle function; called when this task is restarted via [restart].
     */
    open fun onRestart() {}

    /**
     * Defines the info for a [VitalCountdownTask].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val countdown: Long,
        val interval: Long = 1_000L,
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
