package me.vitalframework.tasks

import me.vitalframework.BungeePlugin
import me.vitalframework.BungeeRunnable
import me.vitalframework.BungeeTask
import me.vitalframework.SpigotPlugin
import me.vitalframework.SpigotRunnable
import me.vitalframework.SpigotTask
import net.md_5.bungee.api.ProxyServer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Represents a countdown task that provides customizable countdown functionality.
 *
 * This class serves as a framework for executing countdown-based logic in a controlled manner.
 * It manages the lifecycle of the countdown, including operations such as starting, stopping,
 * and resetting the countdown, as well as handling events that occur on each tick, task expiration,
 * and various lifecycle transitions.
 *
 * The class provides the following configurable behaviors:
 * - Custom logic for task lifecycle events, such as onStart, onStop, onTick, onExpire, etc.
 * - Abstract methods to define specific runnable and task implementations.
 * - Methods to control the state of the countdown task, including start, stop, reset, and restart.
 *
 * Subclasses are expected to provide concrete implementations for the abstract methods to define
 * the specific execution behavior and lifecycle management of the countdown task.
 *
 * @param T The type of the task that executes the countdown.
 * @param R The type of the runnable associated with the countdown process.
 */
abstract class VitalCountdownTask<P, R : Runnable, T>(
    val plugin: P,
) {
    /**
     * Represents the initial countdown value for the countdown task.
     *
     * This value determines the starting point of the countdown and is used
     * to reset or restart the task to its initial state. The countdown process
     * begins from this value and decrements over time until it reaches zero.
     */
    val initialCountdown
        get() = getInfo().countdown

    /**
     * Represents the current countdown value in the `VitalCountdownTask`.
     *
     * This variable holds the remaining time of the countdown process in milliseconds.
     * It is decremented during each tick of the countdown operation, stopping when it reaches zero.
     *
     * The `countdown` variable can be reset to its initial value through the `reset()` method,
     * or modified directly for custom configurations. Changes to this value affect the
     * countdown's behavior and duration.
     *
     * @property countdown The remaining time for the countdown process.
     */
    var countdown = initialCountdown

    /**
     * The interval between each tick of the countdown process, measured in milliseconds.
     *
     * This variable defines the delay duration between successive executions of the task's tick logic.
     * Modifying this value affects how frequently the countdown task updates during its execution.
     *
     * It is recommended to adjust the interval value based on the application's requirements for
     * responsiveness and performance.
     */
    var interval = getInfo().interval

    /**
     * Indicates whether the countdown task is allowed to tick or proceed with its operations.
     *
     * This variable is checked within the `handleTick()` method to determine if the countdown
     * should continue. If set to `false`, the ticking logic will exit early without performing
     * any updates or changes to the countdown state. This allows for temporary pausing or
     * suspending of the countdown process without completely stopping or resetting the task.
     *
     * By default, this property is initialized to `true`, meaning ticking is permitted unless
     * explicitly disabled.
     */
    var allowTick = true

    /**
     * A private mutable property that holds the reference to the runnable instance associated with the countdown task.
     *
     * This property is used internally to manage the lifecycle and execution logic of the countdown process.
     * It is initialized and updated when the task starts or restarts, and is set to null when the task stops or is canceled.
     *
     * Access to modify this property is restricted to internal operations within the class to maintain control
     * over the task's flow and ensure proper synchronization between the runnable's lifecycle and the task's state.
     */
    var runnable: R? = null
        private set

    /**
     * Holds the reference to the current countdown task.
     *
     * This property represents the active task instance associated with the countdown process.
     * It is nullable and is assigned a value when a task is created or started.
     * It will be set to null when the task is stopped or canceled to ensure proper cleanup.
     *
     * This variable is privately set to prevent direct modifications from outside the class,
     * ensuring controlled state management through the class's methods.
     */
    var task: T? = null
        private set

    /**
     * Indicates whether the countdown task is currently running.
     *
     * This property evaluates to `true` if both `runnable` and `task` are not null,
     * signifying that the task is actively executing or ready to execute. Otherwise, it returns `false`,
     * indicating that the countdown process is not in progress.
     */
    val running
        get() = runnable != null && task != null

    /**
     * Starts the countdown task.
     *
     * This method initializes and begins the countdown operation if it is not already running.
     * When invoked, it first verifies if the task is currently active. If the task is inactive,
     * it performs the following steps:
     *
     * - Invokes the `onStart()` method, allowing subclasses to define custom behavior at the start of the task.
     * - Creates a runnable instance using `createRunnable()`, responsible for executing the countdown process.
     * - Creates a countdown task instance using `createTask()`, which manages the task's lifecycle.
     *
     * Later operations that depend on the countdown or the task should use this method
     * to ensure the task is properly initialized and functional before commencement.
     */
    fun start() {
        if (running) return
        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    /**
     * Stops the countdown task if it is currently running.
     *
     * This method ensures that the task's resources are properly released and
     * any ongoing processes associated with the task are terminated. It performs
     * the following operations:
     * - Calls `onStop()` to handle any custom stopping logic.
     * - Cancels the runnable using `cancelRunnable()`.
     * - Cancels the countdown task using `cancelTask()`.
     * - Sets the runnable and task references to `null`.
     *
     * If the task is not currently running, the method exits without performing any actions.
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
     * Resets the state of the countdown task to its initial configuration.
     *
     * This method reassigns the `countdown` value to the initial countdown value
     * stored in `initialCountdown` and invokes the `onReset` method to perform
     * any custom behavior defined for the reset operation.
     *
     * Subclasses can override `onReset` to define specific actions that should
     * take place when the task is reset.
     */
    fun reset() {
        countdown = initialCountdown
        onReset()
    }

    /**
     * Restarts the countdown task.
     *
     * The restart process involves stopping the currently active task, resetting its state,
     * and starting it again. This ensures that the countdown restarts from its initial configuration.
     * Any additional behavior specific to restarting the task is handled in the `onRestart` method.
     * Subclasses may override `onRestart` to implement custom restart logic.
     */
    fun restart() {
        stop()
        reset()
        start()
        onRestart()
    }

    /**
     * Handles the countdown logic for each tick of the task.
     *
     * This method is responsible for managing the countdown behavior, including checking whether
     * ticking is allowed, decrementing the countdown value, triggering the tick logic, and
     * handling task expiration when the countdown reaches zero.
     *
     * Behavior:
     * - If ticking is not allowed (controlled by `allowTick`), the method exits without performing any actions.
     * - If the countdown value has reached or dropped below zero, it stops the countdown task by invoking `stop()`,
     *   triggers the expiration logic with `onExpire()`, and exits.
     * - If ticking is allowed and the countdown is active, the method invokes `onTick()` to handle tick-specific
     *   logic and decreases the countdown value by one.
     */
    fun handleTick() {
        if (!allowTick) return
        if (countdown <= 0) {
            stop()
            onExpire()
            return
        }

        onTick()
        countdown -= 1
    }

    /**
     * Creates and returns a runnable instance associated with the countdown task.
     *
     * Subclasses should implement this method to provide a specific runnable,
     * which defines the core execution logic of the countdown process.
     *
     * @return The runnable instance responsible for executing the countdown logic.
     */
    abstract fun createRunnable(): R

    /**
     * Creates and returns a new instance of a countdown task.
     *
     * This method is abstract and should be implemented in subclasses to define
     * the specific type and behavior of the countdown task being created. The created
     * task instance should be used to manage and execute the countdown process.
     *
     * @return A new instance of the countdown task.
     */
    abstract fun createTask(): T

    /**
     * Cancels the runnable associated with the countdown task.
     *
     * This method is responsible for stopping any ongoing executions
     * of the runnable that was created for managing the countdown process.
     * It can be overridden to implement specific behavior for cancelling the runnable
     * in subclasses. Typically, this would involve ensuring that any scheduled
     * or running instances of the runnable are properly terminated.
     */
    abstract fun cancelRunnable()

    /**
     * Cancels the countdown task.
     *
     * This method is used to terminate the currently running countdown task.
     * Subclasses should provide the implementation to ensure the task is properly canceled,
     * releasing any associated resources and stopping any active processes related to the task.
     */
    abstract fun cancelTask()

    /**
     * Invoked when the countdown task is started.
     *
     * This method is intended to handle any logic required when the countdown task begins.
     * Override this method in subclasses to implement specific actions or setup to be
     * performed at the start of the countdown.
     */
    fun onStart() {}

    /**
     * This method is called periodically during the countdown process.
     *
     * It is invoked at specific intervals defined by the countdown configuration and
     * is usually used to implement any actions or updates that occur on each tick
     * of the countdown. Override this method in a subclass to define custom behavior
     * for each tick.
     */
    fun onTick() {}

    /**
     * Called when the countdown task is stopped.
     *
     * This method is executed during the task's stopping process,
     * allowing the implementation to perform cleanup,
     * release resources, or execute custom logic.
     * Override this method in subclasses if specific actions
     * need to be taken when the task is stopped.
     */
    fun onStop() {}

    /**
     * Invoked when the countdown task expires.
     *
     * This method is called when the countdown reaches its end and is no longer active.
     * Implementations can define behavior to execute upon expiration, such as cleanup operations,
     * triggering specific events, or transitioning to a subsequent state.
     */
    fun onExpire() {}

    /**
     * Called when the countdown task is reset.
     *
     * This method is triggered during the reset operation to allow custom behavior
     * to be implemented. Override this method in subclasses to define actions to
     * be performed specifically when the task is reset to its initial state.
     */
    fun onReset() {}

    /**
     * Invoked when the countdown task is restarted.
     *
     * This method is intended to handle any logic required during the restart of the task,
     * such as reinitializing state, resetting configurations, or performing specific actions
     * necessary to ensure a proper restart of the countdown operation.
     */
    fun onRestart() {}

    /**
     * Annotation to configure countdown-related tasks.
     *
     * This annotation is used to define configuration properties for countdown tasks,
     * such as the initial countdown duration and the interval between ticks.
     *
     * @property countdown The initial countdown duration in milliseconds.
     * @property interval The interval between tick events in milliseconds. Defaults to 1000 milliseconds.
     */
    @Component
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

        override fun cancelRunnable(): Unit = run { runnable?.cancel() }

        override fun cancelTask(): Unit = run { task?.cancel() }
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

        override fun cancelRunnable(): Unit = run { task?.cancel() }

        override fun cancelTask(): Unit = run { task?.cancel() }
    }
}
