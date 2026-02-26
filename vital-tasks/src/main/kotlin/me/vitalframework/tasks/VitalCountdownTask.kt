package me.vitalframework.tasks

import me.vitalframework.BungeePlugin
import me.vitalframework.BungeeRunnable
import me.vitalframework.BungeeTask
import me.vitalframework.SpigotPlugin
import me.vitalframework.SpigotRunnable
import me.vitalframework.SpigotTask
import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
import net.md_5.bungee.api.ProxyServer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Defines a countdown task within the Vital-Framework.
 * A countdown is a [VitalRepeatableTask], with an internal [countdown], decrementing its value by 1 on every tick until it reaches 0.
 * Once the [countdown] reaches 0, its respective [onExpire] function will be called, so consumers can implement their own custom behavior.
 *
 * By default, the class will not be a bean.
 * If dependency injection is wanted or needed, apply the [Component] annotation.
 */
abstract class VitalCountdownTask<P, R : Runnable, T>(
    val plugin: P,
) {
    /**
     * The initial countdown of this countdown task.
     * Will be set to [Info.countdown] upon instantiation.
     */
    val initialCountdown
        get() = getInfo().countdown

    /**
     * The countdown of this countdown task.
     * Will be set to [initialCountdown] upon instantiation.
     */
    var countdown = initialCountdown

    /**
     * The interval between [onTick] calls of this countdown task.
     */
    var interval = getInfo().interval

    /**
     * If this countdown task currently allows ticks.
     */
    var allowTick = true

    /**
     * The runnable of this countdown task.
     */
    var runnable: R? = null
        private set

    /**
     * The task object of this countdown task.
     */
    var task: T? = null
        private set

    /**
     * If this countdown task is currently running.
     */
    val running
        get() = runnable != null && task != null

    /**
     * Starts this countdown task by setting its internal [runnable] using [createRunnable] and [task] using [createTask] and calling [onStart].
     * If [running] is true, this function will pass.
     */
    fun start() {
        if (running) {
            return
        }

        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    /**
     * Stops this countdown task by setting its [runnable] and [task] back to null
     * and calling the following functions in their respective order: [onStop], [cancelRunnable], [cancelTask].
     * If [running] is false, this function will pass.
     */
    fun stop() {
        if (!running) {
            return
        }

        onStop()
        cancelRunnable()
        cancelTask()
        runnable = null
        task = null
    }

    /**
     * Resets this countdown task by setting its internal countdown to [initialCountdown] and calling [onReset].
     */
    fun reset() {
        countdown = initialCountdown
        onReset()
    }

    /**
     * Restarts this countdown task by calling the following functions in their respective order: [stop], [reset], [start], [onRestart].
     */
    fun restart() {
        stop()
        reset()
        start()
        onRestart()
    }

    /**
     * Handles a countdown task tick.
     * Will pass if [allowTick] is false.
     * Calls [stop] and [onExpire] when this countdown task's countdown expires.
     * On every tick, calls [onTick] while decrementing its internal countdown by 1.
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
     * Creates the runnable for this countdown task's task.
     */
    abstract fun createRunnable(): R

    /**
     * Creates the task for this countdown task.
     */
    abstract fun createTask(): T

    /**
     * Cancels this countdown task's timer runnable.
     */
    abstract fun cancelRunnable()

    /**
     * Cancels this countdown task's timer.
     */
    abstract fun cancelTask()

    /**
     * Called when this countdown task is started via [start].
     */
    open fun onStart() {}

    /**
     * Called when this countdown task's timer is ticked with the set [interval].
     */
    open fun onTick() {}

    /**
     * Called when this countdown task is stopped via [stop].
     */
    open fun onStop() {}

    /**
     * Called when this countdown task's countdown expires.
     */
    open fun onExpire() {}

    /**
     * Called when this countdown task is reset via [reset].
     */
    open fun onReset() {}

    /**
     * Called when this countdown task is restarted via [restart].
     */
    open fun onRestart() {}

    companion object {
        /**
         * Retrieves the VitalCountdownTask.Info annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalCountdownTask.Info annotation of this class.
         */
        @JvmStatic
        fun Class<out VitalCountdownTask<*, *, *>>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalCountdownTask.Info annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalCountdownTask.Info annotation of this class.
         */
        @JvmStatic
        fun KClass<out VitalCountdownTask<*, *, *>>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalCountdownTask.Info annotation associated with this instance.
         *
         * @receiver the instance for which the annotation is to be retrieved.
         * @return the VitalCountdownTask.Info annotation of this instance.
         */
        @JvmStatic
        fun VitalCountdownTask<*, *, *>.getInfo(): Info = javaClass.getInfo()
    }

    /**
     * Defines the info for a [VitalCountdownTask].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val countdown: Long,
        val interval: Long = 1_000L,
    )

    /**
     * Represents a specialized countdown task for Spigot-based plugins.
     *
     * This class extends the behavior of a generalized countdown task to integrate
     * with the Spigot framework. It facilitates the periodic execution of tasks
     * using predefined intervals, handles task cancellation, and manages the lifecycle
     * of Spigot-specific task and runnable instances.
     *
     * @param plugin The Spigot plugin instance to associate with this countdown task.
     */
    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalCountdownTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() =
            object : SpigotRunnable() {
                override fun run() = handleTick()
            }

        override fun createTask() = runnable!!.runTaskTimer(plugin, 0L, ((interval / 1000.0) * 20L).toLong())

        override fun cancelRunnable() {
            runnable?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }

    /**
     * Represents a specialized countdown task designed for use within a BungeeCord environment.
     *
     * This class extends the functionality of the `VitalCountdownTask` and provides
     * implementations specific to the BungeeCord framework for creating and managing
     * countdown tasks. It maintains compatibility with the framework's scheduling system
     * while offering lifecycle management for tasks and runnables.
     *
     * Primary functions include:
     * - Creating and managing a runnable responsible for handling countdown ticks.
     * - Scheduling tasks within the BungeeCord scheduler using specific time intervals.
     * - Canceling and cleaning up resources associated with the runnable and task.
     *
     * This class is open, allowing for further extension and customization as required.
     *
     * @constructor Initializes the Bungee countdown task with the provided plugin instance.
     * @param plugin The plugin instance this countdown task is associated with.
     */
    open class Bungee(
        plugin: BungeePlugin,
    ) : VitalCountdownTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
        override fun createRunnable() = BungeeRunnable { handleTick() }

        override fun createTask() = ProxyServer.getInstance().scheduler.schedule(plugin, runnable, 0L, interval, TimeUnit.MILLISECONDS)!!

        override fun cancelRunnable() {
            task?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }
}
