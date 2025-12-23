package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
import org.jetbrains.annotations.Range
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Defines a pageable inventory menu within the Vital-Framework.
 * A pageable inventory can be used to display an inventory, whose content may extend to multiple inventory pages.
 * Useful for creating "scrollable" content inside an inventory GUI, or a shop, selector, etc.
 *
 * ```java
 * public class MyPagedInventory extends VitalPagedInventory {
 *   @Override
 *   public void onPageChange(Int page, Player player) {
 *     // This function is called when a specific player changes the page of his inventory.
 *   }
 *
 *   // ...
 * }
 * ```
 */
abstract class VitalPagedInventory : VitalInventory() {
    private val _pages = mutableMapOf<UUID, Int>()

    /**
     * All [SpigotPlayer]s as their [UUID] and their current page.
     * If no [SpigotPlayer]s have this inventory open, the map will be empty.
     */
    val pages: Map<UUID, Int>
        get() = _pages

    /**
     * The max page this inventory supports.
     */
    var maxPage = 1
        private set

    /**
     * The slot from which the "paged content", so content that will change depending on the page the inventors is on.
     */
    val fromSlot
        get() = getInfo().fromSlot

    /**
     * The slot to which the "paged content" goes, so content
     */
    val toSlot
        get() = getInfo().fromSlot

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
     * Opens the inventory for the specified player with support for pagination and an optional reference to the previous inventory.
     * This implementation allows specifying an initial page and the total content to calculate the maximum number of pages.
     *
     * @param player The player for whom the inventory is being opened.
     * @param previousInventory The player's previous inventory, or null if there is no prior inventory.
     * @param page The initial page to be opened. Defaults to 1 if not specified.
     * @param totalContent The total number of content items, used to determine the maximum page. If null, the maximum page remains unchanged.
     */
    fun open(
        player: SpigotPlayer,
        previousInventory: VitalInventory? = null,
        page: Int = 1,
        totalContent: Int? = null,
    ) {
        super.open(player, previousInventory)
        setPage(page, player, totalContent)
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
    ) {
    }

    companion object {
        /**
         * Retrieves the VitalPagedInventory.Info annotation associated with this class.
         */
        @JvmStatic
        fun Class<out VitalPagedInventory>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalPagedInventory.Info annotation associated with this class.
         */
        @JvmStatic
        fun KClass<out VitalPagedInventory>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalPagedInventory.Info annotation associated with this instance.
         */
        @JvmStatic
        fun VitalPagedInventory.getInfo(): Info = javaClass.getInfo()
    }

    /**
     * Annotation for specifying a range of inventory slots within which an operation or configuration is valid.
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
