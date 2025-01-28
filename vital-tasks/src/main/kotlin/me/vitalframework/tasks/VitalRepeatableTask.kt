package me.vitalframework.tasks

import me.vitalframework.*
import net.md_5.bungee.api.ProxyServer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

abstract class VitalRepeatableTask<P, R : Runnable, T>(
    val plugin: P,
) : RequiresAnnotation<VitalRepeatableTask.Info> {
    var interval: Long
    var allowTick = true
    var runnable: R? = null
    var task: T? = null

    init {
        val info = getRequiredAnnotation()

        interval = info.interval
    }

    override fun requiredAnnotationType() = Info::class.java

    /**
     * Checks if this repeatable task is currently running.
     */
    fun isRunning() = runnable != null && task != null

    /**
     * Starts this repeatable task. If it's already running, this method has no effect.
     */
    fun start() {
        if (isRunning()) {
            return
        }

        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    /**
     * Stops this repeatable task. If it's not running, this method has no effect.
     */
    fun stop() {
        if (!isRunning()) {
            return
        }

        onStop()
        cancelRunnable()
        cancelTask()
        runnable = null
        task = null
    }

    fun handleTick() {
        if (!allowTick) {
            return
        }

        onTick()
    }

    abstract fun createRunnable(): R
    abstract fun createTask(): T
    abstract fun cancelRunnable()
    abstract fun cancelTask()

    fun onStart() {}
    fun onTick() {}
    fun onStop() {}

    /**
     * Annotation used to provide information about the interval of a repeatable task.
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.TYPE)
    annotation class Info(
        val interval: Long,
    )

    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalRepeatableTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() = object : SpigotRunnable() {
            override fun run() {
                handleTick()
            }
        }

        override fun createTask() = runnable!!.runTaskTimer(plugin, 0L, ((interval / 1000.0) * 20L).toLong())

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
        override fun createRunnable(): BungeeRunnable = BungeeRunnable {
            handleTick()
        }

        override fun createTask() =
            ProxyServer.getInstance().scheduler.schedule(plugin, runnable, 0L, interval, TimeUnit.MILLISECONDS)

        override fun cancelRunnable() {
            task?.cancel()
        }

        override fun cancelTask() {
            task?.cancel()
        }
    }
}