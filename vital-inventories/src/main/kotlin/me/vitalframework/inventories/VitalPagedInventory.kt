package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import org.jetbrains.annotations.Range
import java.util.UUID

abstract class VitalPagedInventory : VitalInventory() {
    private val _pages = mutableMapOf<UUID, Int>()

    /**
     * A read-only map that associates each player's unique identifier (UUID) with their current page number.
     *
     * This property is used to track the current page state for each player interacting with the inventory system.
     * The map's keys represent the players, and the values represent the page numbers the players are currently viewing.
     *
     * The map is derived from the internal `_pages` field and provides no direct modification capabilities,
     * ensuring external immutability of the page state.
     */
    val pages: Map<UUID, Int>
        get() = _pages

    /**
     * Represents the maximum number of pages available in the paginated inventory system.
     *
     * This variable is dynamically updated based on the total amount of content
     * and defines the upper limit of pages that can be navigated.
     *
     * The value is initialized to 1 and can only be modified internally.
     */
    var maxPage = 1
        private set

    /**
     * Represents the starting slot index for the current page of the inventory.
     *
     * This variable is used internally to determine the first slot where content
     * should be placed when rendering a paged inventory for a player. The value
     * is updated dynamically based on the current page and the inventory layout.
     *
     * The setter is private, restricting modifications to the internal logic of
     * the class, ensuring consistency with the inventory's pagination behavior.
     */
    val fromSlot
        get() = getRequiredAnnotation<Info>().fromSlot

    /**
     * The ending slot index for a range of slots within the paginated inventory system.
     *
     * This value determines the last slot considered in a given range, typically used when
     * slicing inventory content to display the correct subset for a player's current page.
     *
     * The value is automatically managed, and private setters ensure it cannot be modified directly
     * outside the class's internal logic.
     */
    val toSlot
        get() = getRequiredAnnotation<Info>().fromSlot

    /**
     * Represents the amount of content present on the current page.
     *
     * The value is calculated as the difference between `toSlot` and `fromSlot`, inclusively.
     * This ensures that the calculation accounts for the content range, where `toSlot` is the
     * last slot that contains content, and `fromSlot` is the first slot that contains content.
     */
    val pageContentAmount
        get() = (toSlot + 1) - fromSlot

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
    fun setPage(
        page: Int,
        player: SpigotPlayer,
        totalContent: Int? = null,
    ) {
        if (totalContent != null) {
            updateMaxPage(totalContent)
        }
        val newPage =
            if (page <= 0) {
                1
            } else if (page >= maxPage) {
                maxPage
            } else {
                page
            }
        _pages[player.uniqueId] = newPage
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
    protected fun <T> sliceForPage(
        player: SpigotPlayer,
        list: List<T>,
    ): List<T> {
        val startIndex = (pageContentAmount * ((_pages[player.uniqueId] ?: 1) - 1))
        val endIndex = startIndex + pageContentAmount
        if (startIndex >= list.size || startIndex < 0) return mutableListOf()
        if (endIndex >= list.size) return list.subList(startIndex, list.size)
        return list.subList(startIndex, endIndex)
    }

    final override fun close(player: SpigotPlayer) {
        super.close(player)
        _pages.remove(player.uniqueId)
    }

    /**
     * Opens the inventory for the specified player and optionally retains a reference
     * to the previous inventory state. This implementation also sets the initial page
     * to page 1 for the player.
     *
     * @param player The player for whom the inventory is being opened.
     * @param previousInventory The player's previous inventory, or null if there is no prior inventory.
     */
    final override fun open(
        player: SpigotPlayer,
        previousInventory: VitalInventory?,
    ) {
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
        setPage(_pages[player.uniqueId] ?: 1, player)
    }

    /**
     * Invoked whenever a page change occurs for a specific player in the inventory system.
     *
     * @param page The new page number that the player has navigated to.
     * @param player The player for whom the page change has occurred.
     */
    protected open fun onPageChange(
        page: Int,
        player: SpigotPlayer,
    ) {}

    /**
     * Annotation for specifying a range of inventory slots within which an operation or configuration is valid.
     *
     * @property fromSlot Specifies the starting slot of the range, which must be between 0 and 9 inclusive.
     * @property toSlot Specifies the ending slot of the range, which must be between 0 and 9 inclusive.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val fromSlot:
            @Range(from = 0, to = 9)
            Int = 0,
        val toSlot:
            @Range(from = 0, to = 9)
            Int = 0,
    )
}
