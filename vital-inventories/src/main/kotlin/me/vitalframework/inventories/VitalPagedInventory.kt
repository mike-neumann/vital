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

    /**
     * Updates the maximum number of pages based on the total amount of content.
     *
     * @param totalContent the total number of content items that need to be paginated
     */
    fun updateMaxPage(totalContent: Int) {
        var newMaxPage = totalContent.toDouble() / pageContentAmount.toDouble()

        if (newMaxPage > 1) {
            newMaxPage += 1
        } else {
            newMaxPage = 1.0
        }

        maxPage = newMaxPage.toInt()
    }

    /**
     * Sets the current page for the specified player, updating the maximum page count if total content is provided.
     *
     * @param page The desired page number to set. If the number is out of range, it will be clamped between 1 and the maximum page.
     * @param player The player for whom the page should be set.
     * @param totalContent The total amount of content used to update the maximum page. If null, the maximum page remains unchanged.
     */
    fun setPage(page: Int, player: SpigotPlayer, totalContent: Int? = null) {
        if (totalContent != null) {
            updateMaxPage(totalContent)
        }
        val newPage = if (page <= 0) 1 else if (page >= maxPage) maxPage else page
        _pages[player] = newPage
        onPageChange(newPage, player)
        super.update(player)
    }

    /**
     * Slices a given list to retrieve a subset of elements representing the current page
     * for the specified player based on the player's page state.
     *
     * @param player the player whose page context is used to determine the slice of the list
     * @param list the original list of elements to be paginated
     * @return a sublist containing the elements for the player's current page, or an empty list if the
     *         indices exceed the bounds of the original list
     */
    protected fun <T> sliceForPage(player: SpigotPlayer, list: List<T>): List<T> {
        val startIndex = (pageContentAmount * ((_pages[player] ?: 1) - 1))
        val endIndex = startIndex + pageContentAmount
        if (startIndex >= list.size || startIndex < 0) return mutableListOf()
        if (endIndex >= list.size) return list.subList(startIndex, list.size)
        return list.subList(startIndex, endIndex)
    }

    /**
     * Opens the inventory for the specified player and optionally retains a reference
     * to the previous inventory state. This implementation also sets the initial page
     * to page 1 for the player.
     *
     * @param player The player for whom the inventory is being opened.
     * @param previousInventory The player's previous inventory, or null if there is no prior inventory.
     */
    final override fun open(player: SpigotPlayer, previousInventory: VitalInventory?) {
        super.open(player, previousInventory)
        setPage(1, player)
    }

    /**
     * Updates the inventory for a specific player, ensuring their current page is set.
     * Overrides the base implementation to handle page-specific logic.
     *
     * @param player The player for whom the inventory update is performed.
     */
    final override fun update(player: SpigotPlayer) {
        super.update(player)
        setPage(_pages[player] ?: 1, player)
    }

    /**
     * Invoked whenever a page change occurs for a specific player in the inventory system.
     *
     * @param page The new page number that the player has navigated to.
     * @param player The player for whom the page change has occurred.
     */
    protected open fun onPageChange(page: Int, player: SpigotPlayer) {}

    /**
     * Annotation for specifying a range of inventory slots within which an operation or configuration is valid.
     *
     * @property fromSlot Specifies the starting slot of the range, which must be between 0 and 9 inclusive.
     * @property toSlot Specifies the ending slot of the range, which must be between 0 and 9 inclusive.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(val fromSlot: @Range(from = 0, to = 9) Int = 0, val toSlot: @Range(from = 0, to = 9) Int = 0)
}