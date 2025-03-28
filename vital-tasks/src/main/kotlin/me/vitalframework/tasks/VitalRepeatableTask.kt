package me.vitalframework.tasks

import me.vitalframework.*
import net.md_5.bungee.api.ProxyServer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

abstract class VitalRepeatableTask<P, R : Runnable, T>(val plugin: P) :
    RequiresAnnotation<VitalRepeatableTask.Info> {
    var interval: Long
    var allowTick = true
    var runnable: R? = null
        private set
    var task: T? = null
        private set
    val running get() = runnable != null && task != null

    init {
        val info = getRequiredAnnotation()

        interval = info.interval
    }

    override fun requiredAnnotationType() = Info::class.java

    fun start() {
        if (running) return

        onStart()
        runnable = createRunnable()
        task = createTask()
    }

    fun stop() {
        if (!running) return

        onStop()
        cancelRunnable()
        cancelTask()
        runnable = null
        task = null
    }

    fun handleTick() {
        if (!allowTick) return

        onTick()
    }

    abstract fun createRunnable(): R
    abstract fun createTask(): T
    abstract fun cancelRunnable()
    abstract fun cancelTask()

    fun onStart() {}
    fun onTick() {}
    fun onStop() {}

    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.TYPE)
    annotation class Info(val interval: Long)

    open class Spigot(plugin: SpigotPlugin) : VitalRepeatableTask<SpigotPlugin, SpigotRunnable, SpigotTask>(plugin) {
        override fun createRunnable() = object : SpigotRunnable() {
            override fun run() = handleTick()
        }

        override fun createTask() = runnable!!.runTaskTimer(plugin, 0L, (interval / 1000L) * 20L)
        override fun cancelRunnable(): Unit = run { runnable?.cancel() }
        override fun cancelTask(): Unit = run { task?.cancel() }
    }

    class Bungee(plugin: BungeePlugin) :
        VitalRepeatableTask<BungeePlugin, BungeeRunnable, BungeeTask>(plugin) {
        override fun createRunnable() = BungeeRunnable { handleTick() }
        override fun createTask() = ProxyServer.getInstance().scheduler.schedule(plugin, runnable, 0L, interval, TimeUnit.MILLISECONDS)!!
        override fun cancelRunnable(): Unit = run { task?.cancel() }
        override fun cancelTask(): Unit = run { task?.cancel() }
    }
}