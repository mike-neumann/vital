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
 * Abstract class representing a repeatable task with lifecycle management.
 *
 * This generic class is designed to manage tasks that operate on a repeatable
 * schedule, providing hooks and lifecycle control for starting, stopping,
 * and processing ticks. It maintains the states and operations required for
 * task execution, offering flexibility through abstract methods for specific
 * implementations.
 *
 * @param P The plugin or context instance associated with the task.
 * @param R The type of the runnable defining the task's logic.
 * @param T The type of the task instance used to schedule or manage execution.
 */
abstract class VitalRepeatableTask<P, R : Runnable, T>(
    val plugin: P,
) {
    /**
     * Represents the interval duration in milliseconds.
     * This variable defines the time span or delay used for scheduling repetitive tasks, setting timeouts, or controlling execution intervals in a process.
     */
    var interval = getInfo().interval

    /**
     * Controls whether the task is allowed to process ticks.
     *
     * The `allowTick` variable serves as a gatekeeper within the task lifecycle, determining
     * whether periodic operations (ticks) should occur. When set to `true`, the task processes
     * its ticks as scheduled. If set to `false`, ticking is disabled, and related operations
     * are skipped during the `handleTick` execution.
     *
     * This variable plays an integral role in the task's execution flow, allowing dynamic
     * control over its behavior. It can be modified as needed to temporarily enable or
     * disable ticking without stopping or restarting the entire task.
     */
    var allowTick = true

    /**
     * Represents the current runnable instance associated with the task.
     *
     * This property holds the instance of the runnable that defines the logic to be executed
     * during the lifecycle of the task. It is set during the initialization of the task
     * (via the `start` function) and is cleared when the task is stopped (via the `stop` function).
     *
     * The property is private-set, meaning it can only be modified from within the class
     * it is declared in, ensuring controlled assignment of the runnable.
     *
     * A null value indicates that no runnable is currently active or the task has been stopped.
     */
    var runnable: R? = null
        private set

    /**
     * Represents the currently active scheduled task instance associated with the lifecycle of this object.
     *
     * This property is used internally to store a reference to the task created by the `createTask` method
     * when the `start` method is invoked. It is set to `null` when there is no active task, such as after
     * invoking the `stop` method or before starting the task for the first time.
     *
     * The `task` is managed and controlled through lifecycle functions such as `start`, `stop`,
     * `cancelTask`, and other related operations. Access to this property is restricted to internal
     * management via its private setter.
     */
    var task: T? = null
        private set

    /**
     * Indicates whether the task is currently running.
     *
     * This property evaluates to `true` if both the `runnable` and `task` are non-null, meaning
     * the task has been initialized and its execution has begun. It returns `false` if either
     * the `runnable` or `task` is null, suggesting the task is not active or has been stopped.
     */
    val running
        get() = runnable != null && task != null

    /**
     * Starts this task if not already [running], by creating a [runnable] using [createRunnable] and a [task] by using [createTask].
     * Also calls [onStart] to expose an entry-point for developers.
     */
    fun start() {
        if (running) return
        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    /**
     * Stops the currently active [runnable] and [task] if [running].
     * Also calls [onStop] to expose an entry-point for developers.
     */
    fun stop() {
        if (!running) return
        onStop()
        cancelRunnable()
        cancelTask()
        runnable = null
        task = null
    }

    /**
     * Handles a task-tick by checking if this task currently allows ticks via [allowTick].
     * Then calls the [onTick]-function to expose an entry-point for developers.
     */
    fun handleTick() {
        if (!allowTick) return
        onTick()
    }

    /**
     * Creates the [runnable]-instance for this repeatable task.
     */
    abstract fun createRunnable(): R

    /**
     * Creates the [task]-instance for this repeatable task.
     */
    abstract fun createTask(): T

    /**
     * Cancels the currently active [runnable].
     */
    abstract fun cancelRunnable()

    /**
     * Cancels the currently active [task].
     */
    abstract fun cancelTask()

    /**
     * Called when this task is started ia [start].
     */
    fun onStart() {}

    /**
     * Called on every task-tick.
     * Internally, this function is called, when [handleTick] is called.
     */
    fun onTick() {}

    /**
     * Called when this task is stopped via [stop].
     */
    fun onStop() {}

    companion object {
        /**
         * Retrieves the VitalRepeatableTask.Info annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalRepeatableTask.Info annotation of this class.
         */
        @JvmStatic
        fun Class<out VitalRepeatableTask<*, *, *>>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalRepeatableTask.Info annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalRepeatableTask.Info annotation of this class.
         */
        @JvmStatic
        fun KClass<out VitalRepeatableTask<*, *, *>>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalRepeatableTask.Info annotation associated with this instance.
         *
         * @receiver the instance for which the annotation is to be retrieved.
         * @return the VitalRepeatableTask.Info annotation of this instance.
         */
        @JvmStatic
        fun VitalRepeatableTask<*, *, *>.getInfo(): Info = javaClass.getInfo()
    }

    /**
     * Defines the info for a [VitalRepeatableTask].
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val interval: Long,
    )

    /**
     * Defines a Spigot repeatable task within the Vital-Framework.
     *
     * ```java
     * @VitalRepeatableTask.Info()
     * public class MyRepeatableTask extends VitalRepeatableTask.Spigot {
     *   @Override
     *   public void onStart() {}
     *
     *   @Override
     *   public void onTick() {}
     *
     *   @Override
     *   public void onStop() {}
     * }
     * ```
     */
    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalRepeatableTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() =
            object : SpigotRunnable() {
                override fun run() = handleTick()
            }

        override fun createTask() = runnable!!.runTaskTimer(plugin, 0L, (interval / 1000L) * 20L)

        override fun cancelRunnable() {
            runnable?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }

    /**
     * Represents a repeatable task implementation for the BungeeCord framework.
     *
     * This class extends the `VitalRepeatableTask` framework and provides functionality for
     * executing repeatable tasks tailored to the `BungeePlugin`. The `Bungee` class specifies
     * the creation of a runnable and a scheduled task, as well as their cancellation mechanisms.
     */
    class Bungee(
        plugin: BungeePlugin,
    ) : VitalRepeatableTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
        override fun createRunnable() = BungeeRunnable { handleTick() }

        override fun createTask() =
            ProxyServer.getInstance().scheduler.schedule(
                plugin,
                runnable,
                0L,
                interval,
                TimeUnit.MILLISECONDS,
            )!!

        override fun cancelRunnable() {
            task?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }
}
