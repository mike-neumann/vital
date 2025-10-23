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
     * Starts the execution of the task.
     *
     * This method initiates the task's lifecycle if it is not already running.
     * It first checks whether the task is currently active using the `running` property.
     * If the task is not running, it performs the following steps:
     *
     * 1. Invokes the `onStart` method to trigger any preparation logic before the task begins.
     * 2. Calls the `createRunnable` method to create a new instance of the task's runnable.
     * 3. Calls the `createTask` method to initialize the associated task instance.
     *
     * Once these steps are completed, the task enters the running state.
     * This method does nothing if the task is already active.
     */
    fun start() {
        if (running) return
        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    /**
     * Stops the currently running task, if active.
     *
     * This method ensures that the task's lifecycle is cleanly terminated. If the task is not running,
     * the method will return immediately. Otherwise, it will:
     *
     * - Invoke the `onStop` method, allowing subclasses to perform any necessary cleanup or lifecycle logic.
     * - Cancel the runnable associated with the task by invoking the `cancelRunnable` method.
     * - Cancel the scheduled task by invoking the `cancelTask` method.
     * - Set the `runnable` and `task` fields to `null`, marking the task as no longer active.
     *
     * This method adheres to the lifecycle of the task, ensuring proper termination of related resources
     * and operations.
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
     * Processes a single tick of the task if ticking is allowed.
     *
     * This method checks whether `allowTick` is true before proceeding. If ticking is allowed,
     * it delegates further processing to the `onTick` method. The functionality executed within
     * `onTick` depends on the specific implementation in the subclass.
     *
     * The `handleTick` method plays an integral role in the lifecycle of the task, ensuring that
     * periodic or scheduled logic is executed at the right time. It is typically invoked by the
     * task scheduler’s ticking mechanism.
     *
     * Usage of this function is implicit within the task flow rather than being called directly.
     */
    fun handleTick() {
        if (!allowTick) return
        onTick()
    }

    /**
     * Creates a new instance of the runnable associated with the task.
     *
     * This method is abstract and must be implemented by subclasses to provide
     * a specific implementation of a runnable. The created runnable typically
     * defines the logic to be executed periodically or as part of the task's lifecycle.
     *
     * @return The runnable instance associated with this task.
     */
    abstract fun createRunnable(): R

    /**
     * Creates and initializes a task instance.
     *
     * This method is abstract and must be implemented by subclasses to provide
     * the specific logic for constructing the task object. It is called during
     * the `start` process to define the task that will be executed. The task
     * returned is typically managed within the lifecycle of the broader task system.
     *
     * @return An instance of the task to be executed, as defined by the implementation.
     */
    abstract fun createTask(): T

    /**
     * Cancels the currently active runnable associated with the task.
     *
     * This method is an abstract function to be implemented by subclasses, which should define the
     * specific logic for stopping and cleaning up the execution of the runnable. The function is
     * invoked as part of the task's stoppage process, typically within the `stop` method, to ensure
     * the runnable is properly terminated.
     */
    abstract fun cancelRunnable()

    /**
     * Cancels the currently scheduled task associated with this instance.
     *
     * This method is an abstract function and must be implemented by subclasses
     * to provide the specific logic for halting the execution of the task. Typically,
     * it is used to cleanly stop any ongoing processes or operations initiated by
     * a task that was started.
     *
     * Called as part of the stop process to ensure resources or operations linked
     * to the task are properly terminated and released.
     */
    abstract fun cancelTask()

    /**
     * Hook method invoked when the task is about to start.
     *
     * This method is meant to initialize or prepare any necessary resources
     * or logic before the task begins execution. It is called within the
     * `start` function before the task's runnable and scheduled task are created.
     *
     * Subclasses can optionally override this method to provide specific behavior
     * during the start process.
     */
    fun onStart() {}

    /**
     * Called on every tick of the task when ticking is allowed.
     *
     * This method is executed during the periodic execution of the associated task.
     * It is triggered as part of the task's continuous lifecycle and can be utilized
     * to perform actions or updates that need to occur on each interval.
     *
     * This function is invoked by the task scheduler's ticking mechanism and works
     * alongside the tick handling flow of the task when `allowTick` is true.
     */
    fun onTick() {}

    /**
     * Invoked when the task is being stopped.
     *
     * This method serves as a lifecycle hook for performing any necessary cleanup or actions
     * when the task is stopped. It is typically called before cancelling the runnable and task
     * associated with this object.
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
     * Annotation used to provide metadata for a class that represents a task with a periodic execution cycle.
     *
     * This annotation is applied to task-related classes to define the interval at which the task's
     * periodic or scheduled logic should execute. The value specified in the `interval` parameter
     * represents the execution interval in milliseconds.
     *
     * Classes annotated with `Info` typically serve as components within a larger task management system,
     * leveraging their defined intervals to schedule and control task execution cycles.
     *
     * @property interval The interval, in milliseconds, at which the task is executed.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val interval: Long,
    )

    /**
     * A concrete implementation of `VitalRepeatableTask` specific to the Spigot platform.
     *
     * This class provides platform-specific implementations for managing tasks within a
     * Spigot-based environment. It utilizes `SpigotRunnable` and `SpigotTask` to schedule
     * and execute periodic or repeating logic.
     *
     * The task's lifecycle is managed through methods like `createRunnable`, `createTask`,
     * `cancelRunnable`, and `cancelTask`. These methods ensure that task execution
     * adheres to the scheduling and cancellation behaviors specific to Spigot.
     *
     * @param plugin The Spigot plugin instance to associate with this task.
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
