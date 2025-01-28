package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import org.jetbrains.annotations.Range
import kotlin.math.ceil

/**
 * Used to easily create an interactive paged Inventory Menu.
 * This class extends VitalInventoryMenu for creating paginated menus.
 */
abstract class VitalPagedInventory(previousInventory: VitalInventory?) : VitalInventory(previousInventory) {
    private var page = 1L
    private var maxPage = 1L
    private var fromSlot = 0
    private var toSlot = 0

    /**
     * Constructs a new paged inventory with the specified previous inventory to open after clicking out of inventory menu bounds.
     */
    init {
        val info = javaClass.getAnnotation<Info>(Info::class.java)

        fromSlot = info.fromSlot
        toSlot = info.toSlot
    }

    /**
     * Gets the amount of items required to fill a page from [VitalPagedInventory.fromSlot] to [VitalPagedInventory.toSlot].
     */
    fun getPageContent() = (toSlot + 1 /* since content is INCLUSIVE to the SLOT itself */) - fromSlot

    /**
     * Updates the maxPage indicator with the given total content amount.
     */
    fun updateMaxPage(totalContent: Int) {
        maxPage = ceil(totalContent.toDouble() / getPageContent()).toInt().toLong()
    }

    /**
     * Called when the page of this paged inventory menu changes.
     */
    protected fun onPageChange(page: Long, player: SpigotPlayer) {
    }

    /**
     * Sets the current page of this paged inventory menu.
     */
    fun setPage(page: Long, player: SpigotPlayer) {
        var page = page
        if (page <= 0) {
            page = 1
        }

        if (page >= maxPage) {
            page = maxPage
        }

        this.page = page
        onPageChange(page, player)
        super.update(player)
    }

    /**
     * Slices the given list of items to fit the inventory for the current page
     */
    protected fun <T> sliceForPage(list: MutableList<T>): MutableList<T> {
        val startIndex = (getPageContent() * (page - 1)).toInt()

        if (startIndex >= list.size || startIndex < 0) {
            return mutableListOf()
        }

        val endIndex = startIndex + getPageContent()

        if (endIndex >= list.size) {
            return list.subList(startIndex, list.size)
        }

        return list.subList(startIndex, endIndex)
    }

    override fun open(player: SpigotPlayer) {
        super.open(player)
        setPage(1, player)
    }

    override fun update(player: SpigotPlayer) {
        super.update(player)
        setPage(page, player)
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        /**
         * Defines the starting slot for each page item.
         */
        val fromSlot: @Range(from = 0, to = 9) Int = 0,
        /**
         * Defines the ending slot for each page item.
         */
        val toSlot: @Range(from = 0, to = 9) Int = 0,
    )
}