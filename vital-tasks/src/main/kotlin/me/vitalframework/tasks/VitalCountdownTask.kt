package me.vitalframework.tasks

import me.vitalframework.*
import net.md_5.bungee.api.ProxyServer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

abstract class VitalCountdownTask<P, R : Runnable, T>(
    val plugin: P,
) : RequiresAnnotation<VitalCountdownTask.Info> {
    val initialCountdown: Long
    var countdown: Long
    var interval: Long
    var allowTick = true
    var runnable: R? = null
        private set
    var task: T? = null
        private set

    init {
        val info = getRequiredAnnotation()

        initialCountdown = info.countdown
        countdown = info.countdown
        interval = info.interval
    }

    override fun requiredAnnotationType(): Class<Info> = Info::class.java
    fun isRunning(): Boolean = runnable != null && task != null

    fun start() {
        if (isRunning()) {
            return
        }

        onStart()
        runnable = createRunnable()
        task = createTask()
    }

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

    fun reset() {
        countdown = initialCountdown
        onReset()
    }

    fun restart() {
        stop()
        reset()
        start()
        onRestart()
    }

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

    abstract fun createRunnable(): R
    abstract fun createTask(): T
    abstract fun cancelRunnable()
    abstract fun cancelTask()

    fun onStart() {}
    fun onTick() {}
    fun onStop() {}
    fun onExpire() {}
    fun onReset() {}
    fun onRestart() {}

    @Component
    @Target(AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val countdown: Long,
        val interval: Long = 1_000L,
    )

    open class Spigot(
        plugin: SpigotPlugin,
    ) : VitalCountdownTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
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

    open class Bungee(
        plugin: BungeePlugin,
    ) : VitalCountdownTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
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