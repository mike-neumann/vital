package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import org.jetbrains.annotations.Range

abstract class VitalPagedInventory : VitalInventory() {
    private val _pages = mutableMapOf<SpigotPlayer, Int>()
    val pages: Map<SpigotPlayer, Int> get() = _pages
    var maxPage = 1
        private set
    var fromSlot = 0
        private set
    var toSlot = 0
        private set
    val pageContentAmount get() = (toSlot + 1 /* since content is INCLUSIVE to the SLOT itself */) - fromSlot

    init {
        val info = getRequiredAnnotation<Info>()
        fromSlot = info.fromSlot
        toSlot = info.toSlot
    }

    fun updateMaxPage(totalContent: Int) {
        var newMaxPage = totalContent.toDouble() / pageContentAmount.toDouble()

        if (newMaxPage > 1) {
            newMaxPage += 1
        } else {
            newMaxPage = 1.0
        }

        maxPage = newMaxPage.toInt()
    }

    fun setPage(page: Int, player: SpigotPlayer, totalContent: Int? = null) {
        if (totalContent != null) {
            updateMaxPage(totalContent)
        }
        val newPage = if (page <= 0) 1 else if (page >= maxPage) maxPage else page
        _pages[player] = newPage
        onPageChange(newPage, player)
        super.update(player)
    }

    protected fun <T> sliceForPage(player: SpigotPlayer, list: List<T>): List<T> {
        val startIndex = (pageContentAmount * ((_pages[player] ?: 1) - 1))
        val endIndex = startIndex + pageContentAmount
        if (startIndex >= list.size || startIndex < 0) return mutableListOf()
        if (endIndex >= list.size) return list.subList(startIndex, list.size)
        return list.subList(startIndex, endIndex)
    }

    final override fun open(player: SpigotPlayer, previousInventory: VitalInventory?) {
        super.open(player, previousInventory)
        setPage(1, player)
    }

    final override fun update(player: SpigotPlayer) {
        super.update(player)
        setPage(_pages[player] ?: 1, player)
    }

    protected open fun onPageChange(page: Int, player: SpigotPlayer) {}

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(val fromSlot: @Range(from = 0, to = 9) Int = 0, val toSlot: @Range(from = 0, to = 9) Int = 0)
}