package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalCoreModule.Companion.logger
import java.util.UUID
import kotlin.math.ceil

/**
 * Defines a pageable inventory menu within the Vital-Framework.
 * A pageable inventory can be used to display an inventory, whose content may extend to multiple inventory pages.
 * Useful for creating "scrollable" content inside an inventory GUI, or a shop, selector, etc.
 *
 * By default, the class will be a bean.
 *
 * ```java
 * @VitalPagedInventory.Info(fromSlot = 0, toSlot = 9)
 * @VitalInventory.Info(type = VitalInventory.Type.GENERIC_9X1, name = "MyPagedInventory")
 * public class MyPagedInventory extends VitalPagedInventory {
 *   @Override
 *   public void onPageChange(int page, Player player) {
 *     // This function is called when a specific player changes the page of his inventory.
 *   }
 *
 *   // ...
 * }
 * ```
 */
abstract class VitalPagedInventory : VitalInventory() {
    private val logger = logger()
    private val pages = mutableMapOf<UUID, Int>()
    private val maxPages = mutableMapOf<UUID, Int>()

    init {
        info[Info::class.java] = javaClass.getRequiredAnnotation<Info>()
    }

    /**
     * Gets the page content amount of this inventory defined by ([Info.fromSlot] + 1) - [Info.toSlot].
     */
    fun getPageContentAmount(): Int {
        val info = getInfo(Info::class.java)
        return (info.fromSlot + 1) - info.fromSlot
    }

    /**
     * Gets the current page of the given [playerUniqueId].
     * This function will always at minimum return 1,
     * even if the given player does not have this inventory open.
     */
    fun getPage(playerUniqueId: UUID) = pages[playerUniqueId] ?: 1

    /**
     * Gets the current max page of the given [playerUniqueId].
     * This function will always at minimum return 1,
     * even if the given player does not have this inventory open.
     */
    fun getMaxPage(playerUniqueId: UUID) = maxPages[playerUniqueId] ?: 1

    /**
     * Updates the maximum number of pages based on the total amount of content.
     *
     * @param totalContent the total number of content items that need to be paginated
     */
    fun updateMaxPage(
        playerUniqueId: UUID,
        totalContent: Int,
    ) {
        logger.debug(
            "Updating max page for Vital inventory '$this' and player uuid '$playerUniqueId' from the requested total content amount '$totalContent'.",
        )
        maxPages[playerUniqueId] = ceil((totalContent.toDouble() / getPageContentAmount().toDouble())).toInt().coerceAtLeast(1)
    }

    /**
     * Sets the given [page] for the given [player].
     * Additionally [totalContent] can be defined to automatically calculate the [maxPages] for the given [player].
     *
     * This function will clear all slots defined by [Info.fromSlot] until [Info.toSlot].
     *
     * The [page] will always be at minimum `1`, even when a value lower than that has been provided.
     *
     * Finally, the [onPageChange] lifecycle function is called for the given [player].
     */
    fun setPage(
        page: Int,
        player: SpigotPlayer,
        totalContent: Int? = null,
    ) {
        val loggingContext =
            "Context: player '${player.name}', Vital inventory '$this', " +
                "new page '$page', new total content '$totalContent'."
        logger.debug("Setting page for current Vital inventory and player. $loggingContext")
        val info = getInfo(Info::class.java)

        // Clear old slots.
        logger.debug("Clearing all slots. $loggingContext")
        for (slot in info.fromSlot..info.toSlot) {
            setItem(player, slot, null)
        }

        if (totalContent != null) {
            logger.debug(
                "Total content is set, will use 'updateMaxPage(Integer)' to update the max page for the current player. $loggingContext",
            )
            updateMaxPage(player.uniqueId, totalContent)
        }

        val maxPage = getMaxPage(player.uniqueId)
        val newPage =
            if (page <= 0) {
                1
            } else if (page >= maxPage) {
                maxPage
            } else {
                page
            }

        logger.debug("Final page was determined '$newPage'. $loggingContext")
        pages[player.uniqueId] = newPage

        logger.debug("Calling 'onPageChange(Integer, Player)'. $loggingContext")
        onPageChange(newPage, player)
        super.update(player)
    }

    /**
     * Slices the given [list] to match the [getPageContentAmount] for the current page of the given [player].
     *
     * E.g., if this inventory is configured to have pageable content from slot 0 to 10, [getPageContentAmount] will return `11`.
     * When now passing a [list] of length `20`, this function will return the [list] containing elements from index `0` to `10`.
     * If the given [player] is on page `2`, this function will return the [list] containing elements from index `11` to `19`.
     */
    protected fun <T> sliceForPage(
        player: SpigotPlayer,
        list: List<T>,
    ): List<Pair<Int, T>> {
        val startIndex = (getPageContentAmount() * (getPage(player.uniqueId) - 1))
        val endIndex = startIndex + getPageContentAmount()
        if (startIndex >= list.size || startIndex < 0) {
            return mutableListOf()
        }

        val info = getInfo(Info::class.java)
        if (endIndex >= list.size) {
            return (info.fromSlot..info.toSlot).zip(list.subList(startIndex, list.size))
        }

        return (info.fromSlot..info.toSlot).zip(list.subList(startIndex, endIndex))
    }

    final override fun close(player: SpigotPlayer) {
        super.close(player)
        pages.remove(player.uniqueId)
    }

    /**
     * Opens this inventory for the given [player].
     * Optionally, a [previousInventory] can be passed to make back-traversal possible.
     *
     * When the given [player] clicks outside the current inventory view, he is navigated back to the given [previousInventory].
     * Additionally, this function will set the page for the given [player] to page `1` via [setPage].
     */
    final override fun open(
        player: SpigotPlayer,
        previousInventory: VitalInventory?,
    ) {
        super.open(player, previousInventory)
        setPage(1, player)
    }

    /**
     * Opens this inventory for the given [player].
     * Optionally, a [previousInventory] can be passed to make back-traversal possible.
     *
     * When the given [player] clicks outside the current inventory view, he is navigated back to the given [previousInventory].
     * Additionally, this function will set the page for the given [player] to page `1` via [setPage].
     *
     * This function can also accept [totalContent] to automatically calculate the [maxPages] for the given [player].
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
     * Updates this inventory for the given [player] and additionally calls the [onPageChange] lifecycle.
     */
    final override fun update(player: SpigotPlayer) {
        super.update(player)
        setPage(getPage(player.uniqueId), player)
    }

    /**
     * Lifecycle function; called when the given [page] is updated for the given [player].
     * Override this function to set items that are supposed to change between multiple pages.
     */
    protected open fun onPageChange(
        page: Int,
        player: SpigotPlayer,
    ) {
    }

    /**
     * Defines the info for a [VitalPagedInventory].
     * This annotation must be used on-top of [VitalInventory.Info].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val fromSlot: Int = 0,
        val toSlot: Int = 0,
    )
}
