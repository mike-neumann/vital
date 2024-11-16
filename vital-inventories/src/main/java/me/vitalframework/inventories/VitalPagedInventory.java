package me.vitalframework.inventories;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.inventories.annotation.VitalPagedInventoryInfo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Used to easily create an interactive paged Inventory Menu.
 * This class extends VitalInventoryMenu for creating paginated menus.
 *
 * @author xRa1ny
 */
public abstract class VitalPagedInventory extends VitalInventory {
    /**
     * The current page of this paged inventory menu.
     */
    @Getter
    private long page = 1;

    @Getter
    @Setter
    private int fromSlot = 0;

    @Getter
    @Setter
    private int toSlot = 0;

    @Getter
    @Setter
    private int maxPage = 1;

    /**
     * Constructs a new paged inventory with the specified previous inventory to open after clicking out of inventory menu bounds.
     *
     * @param previousInventory The previous {@link Inventory} to open after clicking out of inventory bounds.
     */
    public VitalPagedInventory(VitalInventory previousInventory) {
        super(previousInventory);

        final var optionalVitalPagedInventoryInfo = Optional.ofNullable(getClass().getAnnotation(VitalPagedInventoryInfo.class));

        optionalVitalPagedInventoryInfo.ifPresent(vitalPagedInventoryInfo -> {
            fromSlot = vitalPagedInventoryInfo.fromSlot();
            toSlot = vitalPagedInventoryInfo.toSlot();
        });
    }

    /**
     * Gets the amount of items required to fill a page from {@link VitalPagedInventory#fromSlot} to {@link VitalPagedInventory#toSlot}.
     *
     * @return The amount.
     */
    public int getPageContent() {
        return (toSlot + 1/* since content is INCLUSIVE to the SLOT itself */) - fromSlot;
    }

    /**
     * Updates the maxPage indicator with the given total content amount.
     *
     * @param totalContent The total amount of content available for paging.
     */
    public void updateMaxPage(int totalContent) {
        maxPage = (int) Math.ceil((double) totalContent / getPageContent());
    }

    /**
     * Called when the page of this paged inventory menu changes.
     *
     * @param page   The new page.
     * @param player The player viewing the inventory.
     */
    protected void onPageChange(long page, @NonNull Player player) {

    }

    /**
     * Sets the current page of this paged inventory menu.
     *
     * @param page   The page.
     * @param player The player viewing the inventory.
     */
    public final void setPage(long page, @NonNull Player player) {
        if (page <= 0) {
            page = 1;
        }

        if (page >= maxPage) {
            page = maxPage;
        }

        this.page = page;
        onPageChange(page, player);
        super.update(player);
    }

    protected <T> List<T> sliceForPage(@NonNull List<T> list) {
        final var startIndex = (int) (getPageContent() * (page - 1));

        if (startIndex >= list.size() || startIndex < 0) {
            return List.of();
        }

        final var endIndex = startIndex + getPageContent();

        if (endIndex >= list.size()) {
            return list.subList(startIndex, list.size());
        }

        return list.subList(startIndex, endIndex);
    }

    @Override
    public void open(Player player) {
        super.open(player);
        setPage(1, player);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        setPage(page, player);
    }
}