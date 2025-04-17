package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import org.jetbrains.annotations.Range
import kotlin.math.ceil

abstract class VitalPagedInventory(previousInventory: VitalInventory?) : VitalInventory(previousInventory) {
    var page = 1L
        private set
    var maxPage = 1L
        private set
    var fromSlot = 0
        private set
    var toSlot = 0
        private set
    val pageContentAmount get() = (toSlot + 1 /* since content is INCLUSIVE to the SLOT itself */) - fromSlot

    init {
        val info = javaClass.getAnnotation(Info::class.java)
        fromSlot = info.fromSlot
        toSlot = info.toSlot
    }

    fun updateMaxPage(totalContent: Int) = run { maxPage = ceil(totalContent.toDouble() / pageContentAmount).toLong() }

    fun setPage(page: Long, player: SpigotPlayer) {
        val newPage = if (page <= 0) 1 else if (page >= maxPage) maxPage else page
        this.page = newPage
        onPageChange(newPage, player)
        super.update(player)
    }

    protected fun <T> sliceForPage(list: List<T>): List<T> {
        val startIndex = (pageContentAmount * (page - 1)).toInt()
        val endIndex = startIndex + pageContentAmount
        if (startIndex >= list.size || startIndex < 0) return mutableListOf()
        if (endIndex >= list.size) return list.subList(startIndex, list.size)
        return list.subList(startIndex, endIndex)
    }

    final override fun open(player: SpigotPlayer) {
        super.open(player)
        setPage(1, player)
    }

    final override fun update(player: SpigotPlayer) {
        super.update(player)
        setPage(page, player)
    }

    protected open fun onPageChange(page: Long, player: SpigotPlayer) {}

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(val fromSlot: @Range(from = 0, to = 9) Int = 0, val toSlot: @Range(from = 0, to = 9) Int = 0)
}